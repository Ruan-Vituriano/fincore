package com.ruan.fincore.dto.investment;

import com.ruan.fincore.enums.InvestmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        String name,
        String ticker,
        InvestmentType type,
        BigDecimal amountInvested,
        BigDecimal currentValue,
        BigDecimal returnAmount,
        BigDecimal returnPercentage,
        LocalDate purchaseDate,
        String notes,
        UUID userId,
        LocalDateTime createdAt
) {
}
