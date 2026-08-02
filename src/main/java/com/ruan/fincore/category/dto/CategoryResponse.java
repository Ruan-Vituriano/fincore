package com.ruan.fincore.category.dto;

import com.ruan.fincore.category.enums.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String icon,
        String color,
        UUID userId,
        LocalDateTime createdAt
) {
}
