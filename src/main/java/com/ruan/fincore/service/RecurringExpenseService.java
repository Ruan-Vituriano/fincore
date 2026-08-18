package com.ruan.fincore.service;

import com.ruan.fincore.dto.recurring.GenerateResponse;
import com.ruan.fincore.dto.recurring.RecurringExpenseRequest;
import com.ruan.fincore.dto.recurring.RecurringExpenseResponse;
import com.ruan.fincore.entity.Account;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.RecurringExpense;
import com.ruan.fincore.entity.Transaction;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.RecurringExpenseMapper;
import com.ruan.fincore.repository.AccountRepository;
import com.ruan.fincore.repository.CategoryRepository;
import com.ruan.fincore.repository.RecurringExpenseRepository;
import com.ruan.fincore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> list(String email) {
        UUID userId = findUserId(email);
        YearMonth current = YearMonth.now();
        List<RecurringExpense> expenses = recurringExpenseRepository.findByUserIdOrderByDayOfMonthAscDescriptionAsc(userId);
        return expenses.stream()
                .map(e -> RecurringExpenseMapper.toResponse(e, isPaidInMonth(e, current)))
                .toList();
    }

    @Transactional
    public RecurringExpenseResponse create(String email, RecurringExpenseRequest request) {
        User user = userService.findByEmail(email);
        Category category = findOwnedCategory(request.categoryId(), user.getId());
        Account account = findOwnedAccount(request.accountId(), user.getId());

        RecurringExpense expense = new RecurringExpense();
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(category);
        expense.setAccount(account);
        expense.setUser(user);
        expense.setDayOfMonth(request.dayOfMonth());
        expense.setIsActive(request.isActive() != null ? request.isActive() : true);

        recurringExpenseRepository.save(expense);
        return RecurringExpenseMapper.toResponse(expense, false);
    }

    @Transactional
    public RecurringExpenseResponse update(String email, UUID id, RecurringExpenseRequest request) {
        UUID userId = findUserId(email);
        RecurringExpense expense = findOwnedExpense(id, userId);
        Category category = findOwnedCategory(request.categoryId(), userId);
        Account account = findOwnedAccount(request.accountId(), userId);

        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(category);
        expense.setAccount(account);
        expense.setDayOfMonth(request.dayOfMonth());
        if (request.isActive() != null) {
            expense.setIsActive(request.isActive());
        }

        YearMonth current = YearMonth.now();
        return RecurringExpenseMapper.toResponse(expense, isPaidInMonth(expense, current));
    }

    @Transactional
    public void delete(String email, UUID id) {
        UUID userId = findUserId(email);
        RecurringExpense expense = findOwnedExpense(id, userId);
        recurringExpenseRepository.delete(expense);
    }

    @Transactional
    public GenerateResponse generateMonthly(String email) {
        UUID userId = findUserId(email);
        User user = userService.findByEmail(email);
        YearMonth current = YearMonth.now();
        LocalDate monthStart = current.atDay(1);
        LocalDate monthEnd = current.atEndOfMonth();

        List<RecurringExpense> activeExpenses = recurringExpenseRepository.findByUserIdAndIsActiveTrueOrderByDayOfMonthAscDescriptionAsc(userId);

        List<String> generated = new ArrayList<>();

        for (RecurringExpense expense : activeExpenses) {
            boolean alreadyExists = transactionRepository.existsByUserIdAndDescriptionAndDateBetweenAndIsRecurringTrue(
                    userId, expense.getDescription(), monthStart, monthEnd);

            if (!alreadyExists) {
                LocalDate transactionDate = current.atDay(Math.min(expense.getDayOfMonth(), current.lengthOfMonth()));

                Transaction transaction = new Transaction();
                transaction.setDescription(expense.getDescription());
                transaction.setAmount(expense.getAmount());
                transaction.setDate(transactionDate);
                transaction.setType(TransactionType.EXPENSE);
                transaction.setCategory(expense.getCategory());
                transaction.setAccount(expense.getAccount());
                transaction.setUser(user);
                transaction.setIsRecurring(true);

                Account account = expense.getAccount();
                account.setBalance(account.getBalance().subtract(expense.getAmount()));

                transactionRepository.save(transaction);
                generated.add(expense.getDescription());
            }
        }

        return new GenerateResponse(generated.size(), generated);
    }

    private boolean isPaidInMonth(RecurringExpense expense, YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        return transactionRepository.existsByUserIdAndDescriptionAndDateBetweenAndIsRecurringTrue(
                expense.getUser().getId(), expense.getDescription(), monthStart, monthEnd);
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private RecurringExpense findOwnedExpense(UUID id, UUID userId) {
        return recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa recorrente não encontrada"));
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
