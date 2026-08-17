package com.ruan.fincore.dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpensesByCategoryResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal total,
        BigDecimal percentage
) {
}
