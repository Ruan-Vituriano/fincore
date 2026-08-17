package com.ruan.fincore.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryExpenseProjection {
    UUID getCategoryId();
    BigDecimal getTotal();
}
