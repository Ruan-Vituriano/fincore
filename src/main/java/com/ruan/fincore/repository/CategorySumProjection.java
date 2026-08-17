package com.ruan.fincore.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategorySumProjection {
    UUID getCategoryId();
    String getCategoryName();
    BigDecimal getTotal();
}
