package com.ruan.fincore.dto.dashboard;

import java.math.BigDecimal;

public record SavingsRateResponse(
        Integer month,
        Integer year,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal savingsRate
) {
}
