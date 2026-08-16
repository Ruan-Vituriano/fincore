package com.ruan.fincore.service;

import com.ruan.fincore.dto.budget.BudgetRequest;
import com.ruan.fincore.dto.budget.BudgetResponse;
import com.ruan.fincore.dto.budget.BudgetSummaryResponse;
import com.ruan.fincore.entity.Budget;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.CategoryType;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.repository.BudgetRepository;
import com.ruan.fincore.repository.CategoryExpenseProjection;
import com.ruan.fincore.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Alimentação");
        category.setType(CategoryType.EXPENSE);
    }

    @Test
    void listShouldReturnBudgets() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setName("Orçamento Alimentação");
        budget.setAmount(BigDecimal.valueOf(800));
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(8);
        budget.setYear(2026);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByUserIdOrderByYearDescMonthDescNameAsc(USER_ID)).thenReturn(List.of(budget));

        List<BudgetResponse> response = budgetService.list("test@example.com", null, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Orçamento Alimentação");
        assertThat(response.get(0).categoryName()).isEqualTo("Alimentação");
    }

    @Test
    void listShouldFilterByMonthAndYear() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setName("Orçamento Alimentação");
        budget.setAmount(BigDecimal.valueOf(800));
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(8);
        budget.setYear(2026);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByNameAsc(USER_ID, 8, 2026)).thenReturn(List.of(budget));

        List<BudgetResponse> response = budgetService.list("test@example.com", 8, 2026);

        assertThat(response).hasSize(1);
    }

    @Test
    void createShouldSaveBudget() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(USER_ID, CATEGORY_ID, 8, 2026)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetRequest request = new BudgetRequest("Orçamento Alimentação", BigDecimal.valueOf(800), CATEGORY_ID, 8, 2026);
        BudgetResponse response = budgetService.create("test@example.com", request);

        assertThat(response.name()).isEqualTo("Orçamento Alimentação");
        assertThat(response.month()).isEqualTo(8);
        assertThat(response.year()).isEqualTo(2026);
    }

    @Test
    void createShouldThrowWhenPeriodExists() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(USER_ID, CATEGORY_ID, 8, 2026)).thenReturn(true);

        BudgetRequest request = new BudgetRequest("Orçamento Alimentação", BigDecimal.valueOf(800), CATEGORY_ID, 8, 2026);

        assertThatThrownBy(() -> budgetService.create("test@example.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria neste período");
    }

    @Test
    void createShouldThrowWhenCategoryNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        BudgetRequest request = new BudgetRequest("Orçamento Alimentação", BigDecimal.valueOf(800), CATEGORY_ID, 8, 2026);

        assertThatThrownBy(() -> budgetService.create("test@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Categoria não encontrada");
    }

    @Test
    void createShouldThrowWhenCategoryBelongsToOtherUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        Category privateCategory = new Category();
        privateCategory.setId(CATEGORY_ID);
        privateCategory.setName("Categoria Privada");
        privateCategory.setUser(otherUser);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(privateCategory));

        BudgetRequest request = new BudgetRequest("Orçamento", BigDecimal.valueOf(500), CATEGORY_ID, 8, 2026);

        assertThatThrownBy(() -> budgetService.create("test@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Categoria não encontrada");
    }

    @Test
    void updateShouldModifyBudget() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setName("Orçamento Antigo");
        budget.setAmount(BigDecimal.valueOf(500));
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(7);
        budget.setYear(2026);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByIdAndUserId(budget.getId(), USER_ID)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(USER_ID, CATEGORY_ID, 8, 2026, budget.getId())).thenReturn(false);

        BudgetRequest request = new BudgetRequest("Orçamento Atualizado", BigDecimal.valueOf(800), CATEGORY_ID, 8, 2026);
        BudgetResponse response = budgetService.update("test@example.com", budget.getId(), request);

        assertThat(response.name()).isEqualTo("Orçamento Atualizado");
        assertThat(response.month()).isEqualTo(8);
    }

    @Test
    void updateShouldThrowWhenBudgetNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        BudgetRequest request = new BudgetRequest("Teste", BigDecimal.valueOf(500), CATEGORY_ID, 8, 2026);

        assertThatThrownBy(() -> budgetService.update("test@example.com", UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Orçamento não encontrado");
    }

    @Test
    void deleteShouldRemoveBudget() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByIdAndUserId(budget.getId(), USER_ID)).thenReturn(Optional.of(budget));

        budgetService.delete("test@example.com", budget.getId());

        verify(budgetRepository).delete(budget);
    }

    @Test
    void summaryShouldCalculateRemainingAndPercentage() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setName("Orçamento Alimentação");
        budget.setAmount(BigDecimal.valueOf(800));
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(8);
        budget.setYear(2026);

        CategoryExpenseProjection projection = mock(CategoryExpenseProjection.class);
        when(projection.getCategoryId()).thenReturn(CATEGORY_ID);
        when(projection.getTotal()).thenReturn(BigDecimal.valueOf(300));

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByNameAsc(USER_ID, 8, 2026)).thenReturn(List.of(budget));
        when(budgetRepository.sumExpensesByCategoryAndPeriod(USER_ID, 8, 2026)).thenReturn(List.of(projection));

        List<BudgetSummaryResponse> response = budgetService.summary("test@example.com", 8, 2026);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).spent()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(response.get(0).remaining()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.get(0).budgeted()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
    void summaryShouldHandleZeroBudget() {
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setName("Orçamento Zerado");
        budget.setAmount(BigDecimal.ZERO);
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(8);
        budget.setYear(2026);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(budgetRepository.findByUserIdAndMonthAndYearOrderByNameAsc(USER_ID, 8, 2026)).thenReturn(List.of(budget));
        when(budgetRepository.sumExpensesByCategoryAndPeriod(USER_ID, 8, 2026)).thenReturn(List.of());

        List<BudgetSummaryResponse> response = budgetService.summary("test@example.com", 8, 2026);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).percentageUsed()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
