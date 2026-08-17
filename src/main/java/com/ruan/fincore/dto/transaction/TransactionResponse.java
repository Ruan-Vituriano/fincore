package com.ruan.fincore.dto.transaction;

import com.ruan.fincore.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        LocalDate date,
        TransactionType type,
        UUID categoryId,
        String categoryName,
        UUID accountId,
        String accountName,
        UUID userId,
        String notes,
        Boolean isRecurring,
        Integer installmentNumber,
        Integer totalInstallments,
        UUID parentTransactionId,
        LocalDateTime createdAt
) {
}
