package com.ruan.fincore.dto.account;

import com.ruan.fincore.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        UUID userId,
        LocalDateTime createdAt
) {
}
