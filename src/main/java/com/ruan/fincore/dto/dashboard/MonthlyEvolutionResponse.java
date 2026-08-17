package com.ruan.fincore.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyEvolutionResponse(
        Integer month,
        Integer year,
        BigDecimal income,
        BigDecimal expense
) {
}
