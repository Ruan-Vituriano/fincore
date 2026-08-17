package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.goal.FinancialGoalResponse;
import com.ruan.fincore.entity.FinancialGoal;

public final class FinancialGoalMapper {

    private FinancialGoalMapper() {
    }

    public static FinancialGoalResponse toResponse(FinancialGoal goal) {
        return new FinancialGoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getUser().getId(),
                goal.getCreatedAt()
        );
    }
}
