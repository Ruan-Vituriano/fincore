package com.ruan.fincore.service;

import com.ruan.fincore.dto.dashboard.DashboardSummaryResponse;
import com.ruan.fincore.dto.dashboard.ExpensesByCategoryResponse;
import com.ruan.fincore.dto.dashboard.MonthlyEvolutionResponse;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.repository.CategorySumProjection;
import com.ruan.fincore.repository.MonthlySumProjection;
import com.ruan.fincore.repository.TransactionRepository;
import com.ruan.fincore.repository.TypeSumProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void summaryShouldCalculateIncomeExpenseAndBalance() {
        TypeSumProjection incomeProjection = mock(TypeSumProjection.class);
        when(incomeProjection.getType()).thenReturn(TransactionType.INCOME);
        when(incomeProjection.getTotal()).thenReturn(BigDecimal.valueOf(5000));

        TypeSumProjection expenseProjection = mock(TypeSumProjection.class);
        when(expenseProjection.getType()).thenReturn(TransactionType.EXPENSE);
        when(expenseProjection.getTotal()).thenReturn(BigDecimal.valueOf(3000));

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumByTypeAndPeriod(USER_ID, 8, 2026))
                .thenReturn(List.of(incomeProjection, expenseProjection));

        DashboardSummaryResponse response = dashboardService.summary("test@example.com", 8, 2026);

        assertThat(response.income()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.expense()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void summaryShouldReturnZerosWhenNoTransactions() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumByTypeAndPeriod(USER_ID, 1, 2025)).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.summary("test@example.com", 1, 2025);

        assertThat(response.income()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.expense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void expensesByCategoryShouldReturnCategoriesOrderedByTotal() {
        CategorySumProjection food = mock(CategorySumProjection.class);
        when(food.getCategoryId()).thenReturn(UUID.randomUUID());
        when(food.getCategoryName()).thenReturn("Alimentação");
        when(food.getTotal()).thenReturn(BigDecimal.valueOf(800));

        CategorySumProjection transport = mock(CategorySumProjection.class);
        when(transport.getCategoryId()).thenReturn(UUID.randomUUID());
        when(transport.getCategoryName()).thenReturn("Transporte");
        when(transport.getTotal()).thenReturn(BigDecimal.valueOf(200));

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumExpensesByCategoryAndPeriod(USER_ID, 8, 2026))
                .thenReturn(List.of(food, transport));

        List<ExpensesByCategoryResponse> response = dashboardService.expensesByCategory("test@example.com", 8, 2026);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).categoryName()).isEqualTo("Alimentação");
        assertThat(response.get(0).percentage()).isEqualByComparingTo(BigDecimal.valueOf(80.0000));
        assertThat(response.get(1).categoryName()).isEqualTo("Transporte");
        assertThat(response.get(1).percentage()).isEqualByComparingTo(BigDecimal.valueOf(20.0000));
    }

    @Test
    void expensesByCategoryShouldReturnEmptyWhenNoExpenses() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumExpensesByCategoryAndPeriod(USER_ID, 1, 2025)).thenReturn(List.of());

        List<ExpensesByCategoryResponse> response = dashboardService.expensesByCategory("test@example.com", 1, 2025);

        assertThat(response).isEmpty();
    }

    @Test
    void monthlyEvolutionShouldReturnMonthsInDescendingOrder() {
        LocalDate now = LocalDate.now();
        MonthlySumProjection currentMonth = mock(MonthlySumProjection.class);
        when(currentMonth.getMonth()).thenReturn(now.getMonthValue());
        when(currentMonth.getYear()).thenReturn(now.getYear());
        when(currentMonth.getType()).thenReturn("INCOME");
        when(currentMonth.getTotal()).thenReturn(BigDecimal.valueOf(5000));

        MonthlySumProjection lastMonth = mock(MonthlySumProjection.class);
        when(lastMonth.getMonth()).thenReturn(now.minusMonths(1).getMonthValue());
        when(lastMonth.getYear()).thenReturn(now.minusMonths(1).getYear());
        when(lastMonth.getType()).thenReturn("EXPENSE");
        when(lastMonth.getTotal()).thenReturn(BigDecimal.valueOf(3000));

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumByTypeAndMonth(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of(currentMonth, lastMonth));

        List<MonthlyEvolutionResponse> response = dashboardService.monthlyEvolution("test@example.com", 2);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).month()).isEqualTo(now.getMonthValue());
        assertThat(response.get(0).year()).isEqualTo(now.getYear());
        assertThat(response.get(0).income()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.get(0).expense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.get(1).income()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.get(1).expense()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void monthlyEvolutionShouldHandleMissingMonths() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(transactionRepository.sumByTypeAndMonth(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<MonthlyEvolutionResponse> response = dashboardService.monthlyEvolution("test@example.com", 3);

        assertThat(response).hasSize(3);
        for (MonthlyEvolutionResponse r : response) {
            assertThat(r.income()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(r.expense()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
