package com.ruan.fincore.dto.budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        String name,
        BigDecimal amount,
        UUID categoryId,
        String categoryName,
        UUID userId,
        Integer month,
        Integer year,
        LocalDateTime createdAt
) {
}
