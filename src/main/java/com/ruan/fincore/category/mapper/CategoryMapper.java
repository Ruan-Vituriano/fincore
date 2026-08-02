package com.ruan.fincore.category.mapper;

import com.ruan.fincore.category.dto.CategoryResponse;
import com.ruan.fincore.category.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getUser() != null ? category.getUser().getId() : null,
                category.getCreatedAt()
        );
    }
}
