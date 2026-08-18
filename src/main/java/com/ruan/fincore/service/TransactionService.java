package com.ruan.fincore.service;

import com.ruan.fincore.dto.transaction.TransactionRequest;
import com.ruan.fincore.dto.transaction.TransactionResponse;
import com.ruan.fincore.entity.Account;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.Transaction;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.CategoryType;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.TransactionMapper;
import com.ruan.fincore.repository.AccountRepository;
import com.ruan.fincore.repository.CategoryRepository;
import com.ruan.fincore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(String email, LocalDate dateFrom, LocalDate dateTo,
                                          UUID categoryId, UUID accountId, TransactionType type) {
        UUID userId = findUserId(email);
        return transactionRepository.findByFilters(userId, dateFrom, dateTo, categoryId, accountId, type).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<TransactionResponse> create(String email, TransactionRequest request) {
        User user = userService.findByEmail(email);
        Category category = findOwnedCategory(request.categoryId(), user.getId());
        Account account = findOwnedAccount(request.accountId(), user.getId());
        TransactionType type = toTransactionType(category.getType());
        Integer totalInstallments = request.totalInstallments();

        if (totalInstallments != null && totalInstallments > 1) {
            return createInstallments(user, category, account, request, type, totalInstallments);
        }

        Transaction transaction = buildTransaction(user, category, account, request, type, null, 1, 1);
        applyBalance(account, type, transaction.getAmount());
        transactionRepository.save(transaction);
        return List.of(TransactionMapper.toResponse(transaction));
    }

    @Transactional
    public TransactionResponse update(String email, UUID id, TransactionRequest request, boolean applyToAll) {
        UUID userId = findUserId(email);
        Transaction transaction = findOwnedTransaction(id, userId);
        Category category = findOwnedCategory(request.categoryId(), userId);
        Account account = findOwnedAccount(request.accountId(), userId);
        TransactionType newType = toTransactionType(category.getType());

        if (applyToAll && transaction.getParentTransaction() == null && transaction.getTotalInstallments() != null
                && transaction.getTotalInstallments() > 1) {
            return updateSeries(transaction, userId, request, category, account, newType);
        }

        revertBalance(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        applyRequest(transaction, request, category, account, newType);
        applyBalance(account, newType, transaction.getAmount());
        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    public void delete(String email, UUID id, boolean applyToAll) {
        UUID userId = findUserId(email);
        Transaction transaction = findOwnedTransaction(id, userId);

        if (applyToAll && transaction.getParentTransaction() == null && transaction.getTotalInstallments() != null
                && transaction.getTotalInstallments() > 1) {
            deleteSeries(transaction, userId);
            return;
        }

        revertBalance(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listInstallments(String email, UUID parentId) {
        UUID userId = findUserId(email);
        findOwnedTransaction(parentId, userId);
        return transactionRepository.findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(parentId, userId)
                .stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    private List<TransactionResponse> createInstallments(User user, Category category, Account account,
                                                         TransactionRequest request, TransactionType type,
                                                         int totalInstallments) {
        BigDecimal totalAmount = request.amount();
        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal remainder = totalAmount.subtract(installmentAmount.multiply(BigDecimal.valueOf(totalInstallments)));

        Transaction parent = buildTransaction(user, category, account, request, type, null, 1, totalInstallments);
        parent.setInstallmentNumber(1);
        parent.setAmount(installmentAmount.add(remainder));
        applyBalance(account, type, parent.getAmount());
        transactionRepository.save(parent);

        List<TransactionResponse> responses = new ArrayList<>();
        responses.add(TransactionMapper.toResponse(parent));

        for (int i = 2; i <= totalInstallments; i++) {
            Transaction installment = buildTransaction(user, category, account, request, type, parent, i, totalInstallments);
            installment.setAmount(installmentAmount);
            installment.setDate(request.date().plusMonths(i - 1));
            applyBalance(account, type, installment.getAmount());
            transactionRepository.save(installment);
            responses.add(TransactionMapper.toResponse(installment));
        }

        return responses;
    }

    private Transaction buildTransaction(User user, Category category, Account account,
                                         TransactionRequest request, TransactionType type,
                                         Transaction parent, int installmentNumber, int totalInstallments) {
        Transaction transaction = new Transaction();
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setDate(request.date());
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAccount(account);
        transaction.setUser(user);
        transaction.setNotes(request.notes());
        transaction.setIsRecurring(request.isRecurring() != null && request.isRecurring());
        transaction.setInstallmentNumber(installmentNumber);
        transaction.setTotalInstallments(totalInstallments);
        transaction.setParentTransaction(parent);
        return transaction;
    }

    private void applyRequest(Transaction transaction, TransactionRequest request, Category category,
                              Account account, TransactionType type) {
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setDate(request.date());
        transaction.setCategory(category);
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setNotes(request.notes());
        transaction.setIsRecurring(request.isRecurring() != null && request.isRecurring());
    }

    private TransactionResponse updateSeries(Transaction root, UUID userId, TransactionRequest request,
                                             Category category, Account account, TransactionType newType) {
        List<Transaction> series = transactionRepository
                .findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(root.getId(), userId);

        revertBalance(root.getAccount(), root.getType(), root.getAmount());
        applyRequest(root, request, category, account, newType);
        applyBalance(account, newType, root.getAmount());

        for (Transaction t : series) {
            revertBalance(t.getAccount(), t.getType(), t.getAmount());
            t.setCategory(category);
            t.setAccount(account);
            t.setType(newType);
            t.setDescription(request.description());
            t.setNotes(request.notes());
            t.setIsRecurring(request.isRecurring() != null && request.isRecurring());
            applyBalance(account, newType, t.getAmount());
        }

        return TransactionMapper.toResponse(root);
    }

    private void deleteSeries(Transaction root, UUID userId) {
        List<Transaction> series = transactionRepository
                .findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(root.getId(), userId);

        for (Transaction t : series) {
            revertBalance(t.getAccount(), t.getType(), t.getAmount());
            transactionRepository.delete(t);
        }

        revertBalance(root.getAccount(), root.getType(), root.getAmount());
        transactionRepository.delete(root);
    }

    private void applyBalance(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    private void revertBalance(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private TransactionType toTransactionType(CategoryType categoryType) {
        return TransactionType.valueOf(categoryType.name());
    }

    private Transaction findOwnedTransaction(UUID id, UUID userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }

    private Category findOwnedCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        boolean accessible = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!accessible) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        return category;
    }

    private Account findOwnedAccount(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }
}
