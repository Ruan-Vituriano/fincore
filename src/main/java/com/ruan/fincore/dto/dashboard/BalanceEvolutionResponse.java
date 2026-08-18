package com.ruan.fincore.dto.dashboard;

import java.math.BigDecimal;

public record BalanceEvolutionResponse(
        Integer month,
        Integer year,
        BigDecimal balance
) {
}
