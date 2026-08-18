package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.investment.InvestmentResponse;
import com.ruan.fincore.entity.Investment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class InvestmentMapper {

    private InvestmentMapper() {
    }

    public static InvestmentResponse toResponse(Investment investment) {
        BigDecimal returnAmount = investment.getCurrentValue().subtract(investment.getAmountInvested());
        BigDecimal returnPercentage = investment.getAmountInvested().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : returnAmount.divide(investment.getAmountInvested(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new InvestmentResponse(
                investment.getId(),
                investment.getName(),
                investment.getTicker(),
                investment.getType(),
                investment.getAmountInvested(),
                investment.getCurrentValue(),
                returnAmount,
                returnPercentage,
                investment.getPurchaseDate(),
                investment.getNotes(),
                investment.getUser().getId(),
                investment.getCreatedAt()
        );
    }
}
