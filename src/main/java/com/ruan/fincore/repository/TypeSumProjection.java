package com.ruan.fincore.repository;

import com.ruan.fincore.enums.TransactionType;

import java.math.BigDecimal;

public interface TypeSumProjection {
    TransactionType getType();
    BigDecimal getTotal();
}
