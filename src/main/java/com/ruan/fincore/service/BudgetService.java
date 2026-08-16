package com.ruan.fincore.service;

import com.ruan.fincore.dto.budget.BudgetRequest;
import com.ruan.fincore.dto.budget.BudgetResponse;
import com.ruan.fincore.dto.budget.BudgetSummaryResponse;
import com.ruan.fincore.entity.Budget;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.BudgetMapper;
import com.ruan.fincore.repository.BudgetRepository;
import com.ruan.fincore.repository.CategoryExpenseProjection;
import com.ruan.fincore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(String email, Integer month, Integer year) {
        UUID userId = findUserId(email);
        List<Budget> budgets = (month != null && year != null)
                ? budgetRepository.findByUserIdAndMonthAndYearOrderByNameAsc(userId, month, year)
                : budgetRepository.findByUserIdOrderByYearDescMonthDescNameAsc(userId);
        return budgets.stream().map(BudgetMapper::toResponse).toList();
    }

    @Transactional
    public BudgetResponse create(String email, BudgetRequest request) {
        User user = userService.findByEmail(email);
        Category category = findAccessibleCategory(request.categoryId(), user.getId());
        validateUniquePeriod(user.getId(), category.getId(), request.month(), request.year(), null);
        Budget budget = new Budget();
        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setCategory(category);
        budget.setUser(user);
        budget.setMonth(request.month());
        budget.setYear(request.year());
        return BudgetMapper.toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(String email, UUID id, BudgetRequest request) {
        UUID userId = findUserId(email);
        Budget budget = findOwnedBudget(id, userId);
        Category category = findAccessibleCategory(request.categoryId(), userId);
        validateUniquePeriod(userId, category.getId(), request.month(), request.year(), id);
        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setCategory(category);
        budget.setMonth(request.month());
        budget.setYear(request.year());
        return BudgetMapper.toResponse(budget);
    }

    @Transactional
    public void delete(String email, UUID id) {
        Budget budget = findOwnedBudget(id, findUserId(email));
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public List<BudgetSummaryResponse> summary(String email, Integer month, Integer year) {
        UUID userId = findUserId(email);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYearOrderByNameAsc(userId, month, year);
        Map<UUID, BigDecimal> spentByCategory = new HashMap<>();
        for (CategoryExpenseProjection p : budgetRepository.sumExpensesByCategoryAndPeriod(userId, month, year)) {
            spentByCategory.put(p.getCategoryId(), p.getTotal());
        }
        return budgets.stream()
                .map(b -> toSummary(b, spentByCategory.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO)))
                .toList();
    }

    private BudgetSummaryResponse toSummary(Budget budget, BigDecimal spent) {
        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return new BudgetSummaryResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                spent,
                remaining,
                percentageUsed
        );
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private Budget findOwnedBudget(UUID id, UUID userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento não encontrado"));
    }

    private Category findAccessibleCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        boolean accessible = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!accessible) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        return category;
    }

    private void validateUniquePeriod(UUID userId, UUID categoryId, Integer month, Integer year, UUID id) {
        boolean exists = id == null
                ? budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)
                : budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(userId, categoryId, month, year, id);
        if (exists) {
            throw new BusinessException("Já existe um orçamento para esta categoria neste período");
        }
    }
}
