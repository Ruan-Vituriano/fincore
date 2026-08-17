package com.ruan.fincore.dto.goal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialGoalResponse(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        UUID userId,
        LocalDateTime createdAt
) {
}
