package com.ruan.fincore.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
