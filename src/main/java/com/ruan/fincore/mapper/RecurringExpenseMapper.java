package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.recurring.RecurringExpenseResponse;
import com.ruan.fincore.entity.RecurringExpense;

public final class RecurringExpenseMapper {

    private RecurringExpenseMapper() {
    }

    public static RecurringExpenseResponse toResponse(RecurringExpense expense, boolean paidInCurrentMonth) {
        return new RecurringExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getAccount().getId(),
                expense.getAccount().getName(),
                expense.getUser().getId(),
                expense.getDayOfMonth(),
                expense.getIsActive(),
                paidInCurrentMonth,
                expense.getCreatedAt()
        );
    }
}
