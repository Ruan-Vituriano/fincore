package com.ruan.fincore.dto.recurring;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        UUID categoryId,
        String categoryName,
        UUID accountId,
        String accountName,
        UUID userId,
        Integer dayOfMonth,
        Boolean isActive,
        Boolean paidInCurrentMonth,
        LocalDateTime createdAt
) {
}
