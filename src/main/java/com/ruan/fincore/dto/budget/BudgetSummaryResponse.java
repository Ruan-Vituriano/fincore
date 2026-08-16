package com.ruan.fincore.dto.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetSummaryResponse(
        UUID budgetId,
        UUID categoryId,
        String categoryName,
        BigDecimal budgeted,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal percentageUsed
) {
}
