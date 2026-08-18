package com.ruan.fincore.dto.investment;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioSummaryResponse(
        BigDecimal totalInvested,
        BigDecimal totalCurrentValue,
        BigDecimal totalReturnAmount,
        BigDecimal totalReturnPercentage,
        Map<String, AllocationByType> allocationByType
) {
}
