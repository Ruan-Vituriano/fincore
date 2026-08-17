package com.ruan.fincore.service;

import com.ruan.fincore.dto.transaction.TransactionRequest;
import com.ruan.fincore.dto.transaction.TransactionResponse;
import com.ruan.fincore.entity.Account;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.Transaction;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.AccountType;
import com.ruan.fincore.enums.CategoryType;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.repository.AccountRepository;
import com.ruan.fincore.repository.CategoryRepository;
import com.ruan.fincore.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        expenseCategory = new Category();
        expenseCategory.setId(CATEGORY_ID);
        expenseCategory.setName("Alimentação");
        expenseCategory.setType(CategoryType.EXPENSE);
        expenseCategory.setUser(user);

        incomeCategory = new Category();
        incomeCategory.setId(UUID.fromString("00000000-0000-0000-0000-000000000004"));
        incomeCategory.setName("Salário");
        incomeCategory.setType(CategoryType.INCOME);
        incomeCategory.setUser(user);

        account = new Account();
        account.setId(ACCOUNT_ID);
        account.setName("Nubank");
        account.setType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setUser(user);
    }

    @Test
    void listShouldReturnTransactions() {
        Transaction transaction = transaction(BigDecimal.valueOf(50), TransactionType.EXPENSE);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.findByFilters(USER_ID, null, null, null, null, null))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> response = transactionService.list("test@example.com", null, null, null, null, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void createShouldReturnSingleTransaction() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(expenseCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Almoço", BigDecimal.valueOf(35),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, null);

        List<TransactionResponse> response = transactionService.create("test@example.com", request);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).description()).isEqualTo("Almoço");
        assertThat(response.get(0).type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.get(0).installmentNumber()).isEqualTo(1);
        assertThat(response.get(0).totalInstallments()).isEqualTo(1);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(965));
    }

    @Test
    void createShouldGenerateInstallments() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(expenseCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Notebook", BigDecimal.valueOf(1200),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, 3);

        List<TransactionResponse> response = transactionService.create("test@example.com", request);

        assertThat(response).hasSize(3);
        assertThat(response.get(0).installmentNumber()).isEqualTo(1);
        assertThat(response.get(0).totalInstallments()).isEqualTo(3);
        assertThat(response.get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(response.get(1).installmentNumber()).isEqualTo(2);
        assertThat(response.get(1).amount()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(response.get(1).date()).isEqualTo(LocalDate.of(2024, 2, 15));
        assertThat(response.get(2).installmentNumber()).isEqualTo(3);
        assertThat(response.get(2).amount()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(response.get(2).date()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000 - 1200));
    }

    @Test
    void createShouldAbsorbRoundingDifference() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(expenseCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Curso", BigDecimal.valueOf(100),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, 3);

        List<TransactionResponse> response = transactionService.create("test@example.com", request);

        assertThat(response.get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(33.34));
        assertThat(response.get(1).amount()).isEqualByComparingTo(BigDecimal.valueOf(33.33));
        assertThat(response.get(2).amount()).isEqualByComparingTo(BigDecimal.valueOf(33.33));
    }

    @Test
    void updateShouldAdjustBalance() {
        Transaction transaction = transaction(BigDecimal.valueOf(50), TransactionType.EXPENSE);
        Account newAccount = account("Inter", AccountType.SAVINGS, BigDecimal.valueOf(2000));
        Category newCategory = category("Transporte", CategoryType.EXPENSE);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findByIdAndUserId(newCategory.getId(), USER_ID)).thenReturn(Optional.of(newCategory));
        when(accountRepository.findByIdAndUserId(newAccount.getId(), USER_ID)).thenReturn(Optional.of(newAccount));

        TransactionRequest request = new TransactionRequest("Uber", BigDecimal.valueOf(25),
                LocalDate.of(2024, 1, 20), newCategory.getId(), newAccount.getId(), null, null, null);

        transactionService.update("test@example.com", transaction.getId(), request, false);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1050));
        assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1975));
    }

    @Test
    void updateWithApplyToAllShouldUpdateSeries() {
        Transaction root = transaction(BigDecimal.valueOf(400.01), TransactionType.EXPENSE);
        root.setTotalInstallments(3);
        root.setInstallmentNumber(1);

        Transaction child1 = transaction(BigDecimal.valueOf(400), TransactionType.EXPENSE);
        child1.setParentTransaction(root);
        child1.setInstallmentNumber(2);
        child1.setAccount(account);

        Transaction child2 = transaction(BigDecimal.valueOf(400), TransactionType.EXPENSE);
        child2.setParentTransaction(root);
        child2.setInstallmentNumber(3);
        child2.setAccount(account);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(root.getId(), USER_ID)).thenReturn(Optional.of(root));
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(expenseCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(root.getId(), USER_ID))
                .thenReturn(List.of(child1, child2));

        TransactionRequest request = new TransactionRequest("Curso Atualizado", BigDecimal.valueOf(100),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, null);

        TransactionResponse response = transactionService.update("test@example.com", root.getId(), request, true);

        assertThat(response.description()).isEqualTo("Curso Atualizado");
        assertThat(root.getDescription()).isEqualTo("Curso Atualizado");
        assertThat(child1.getDescription()).isEqualTo("Curso Atualizado");
        assertThat(child2.getDescription()).isEqualTo("Curso Atualizado");
    }

    @Test
    void deleteShouldReverseBalance() {
        Transaction transaction = transaction(BigDecimal.valueOf(50), TransactionType.EXPENSE);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID)).thenReturn(Optional.of(transaction));

        transactionService.delete("test@example.com", transaction.getId(), false);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1050));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteWithApplyToAllShouldDeleteSeries() {
        Transaction root = transaction(BigDecimal.valueOf(400.01), TransactionType.EXPENSE);
        root.setTotalInstallments(3);
        root.setInstallmentNumber(1);

        Transaction child1 = transaction(BigDecimal.valueOf(400), TransactionType.EXPENSE);
        child1.setParentTransaction(root);
        child1.setInstallmentNumber(2);
        child1.setAccount(account);

        Transaction child2 = transaction(BigDecimal.valueOf(400), TransactionType.EXPENSE);
        child2.setParentTransaction(root);
        child2.setInstallmentNumber(3);
        child2.setAccount(account);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(root.getId(), USER_ID)).thenReturn(Optional.of(root));
        when(transactionRepository.findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(root.getId(), USER_ID))
                .thenReturn(List.of(child1, child2));

        transactionService.delete("test@example.com", root.getId(), true);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000).add(BigDecimal.valueOf(400.01).add(BigDecimal.valueOf(400)).add(BigDecimal.valueOf(400))));
        verify(transactionRepository).delete(root);
        verify(transactionRepository).delete(child1);
        verify(transactionRepository).delete(child2);
    }

    @Test
    void createShouldThrowWhenCategoryNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest("Teste", BigDecimal.valueOf(50),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, null);

        assertThatThrownBy(() -> transactionService.create("test@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Categoria não encontrada");
    }

    @Test
    void createShouldThrowWhenAccountNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(expenseCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest("Teste", BigDecimal.valueOf(50),
                LocalDate.of(2024, 1, 15), CATEGORY_ID, ACCOUNT_ID, null, null, null);

        assertThatThrownBy(() -> transactionService.create("test@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");
    }

    @Test
    void createIncomeShouldIncreaseBalance() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(incomeCategory.getId(), USER_ID)).thenReturn(Optional.of(incomeCategory));
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Salário", BigDecimal.valueOf(5000),
                LocalDate.of(2024, 1, 5), incomeCategory.getId(), ACCOUNT_ID, null, null, null);

        transactionService.create("test@example.com", request);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    private Transaction transaction(BigDecimal amount, TransactionType type) {
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID());
        t.setAmount(amount);
        t.setType(type);
        t.setAccount(account);
        t.setUser(user);
        t.setCategory(type == TransactionType.EXPENSE ? expenseCategory : incomeCategory);
        t.setDate(LocalDate.of(2024, 1, 15));
        t.setDescription("Test");
        t.setIsRecurring(false);
        return t;
    }

    private Account account(String name, AccountType type, BigDecimal balance) {
        Account a = new Account();
        a.setId(UUID.randomUUID());
        a.setName(name);
        a.setType(type);
        a.setBalance(balance);
        a.setUser(user);
        return a;
    }

    private Category category(String name, CategoryType type) {
        Category c = new Category();
        c.setId(UUID.randomUUID());
        c.setName(name);
        c.setType(type);
        c.setUser(user);
        return c;
    }
}
