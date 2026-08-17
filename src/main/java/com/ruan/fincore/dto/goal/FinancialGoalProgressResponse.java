package com.ruan.fincore.dto.goal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialGoalProgressResponse(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal percentageAchieved,
        LocalDate deadline
) {
}
