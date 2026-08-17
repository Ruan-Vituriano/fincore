package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.budget.BudgetResponse;
import com.ruan.fincore.entity.Budget;

public final class BudgetMapper {

    private BudgetMapper() {
    }

    public static BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getUser().getId(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCreatedAt()
        );
    }
}
