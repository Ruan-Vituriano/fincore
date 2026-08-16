package com.ruan.fincore.dto.category;

import com.ruan.fincore.enums.CategoryType;

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
