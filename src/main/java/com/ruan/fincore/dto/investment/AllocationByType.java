package com.ruan.fincore.dto.investment;

import java.math.BigDecimal;

public record AllocationByType(
        BigDecimal amount,
        BigDecimal percentage
) {
}
