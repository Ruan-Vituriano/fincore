package com.ruan.fincore.repository;

import java.math.BigDecimal;

public interface MonthlySumProjection {
    Integer getMonth();
    Integer getYear();
    String getType();
    BigDecimal getTotal();
}
