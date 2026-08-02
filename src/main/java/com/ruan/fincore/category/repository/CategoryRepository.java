package com.ruan.fincore.category.repository;

import com.ruan.fincore.category.entity.Category;
import com.ruan.fincore.category.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserIdOrUserIdIsNullOrderByName(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByNameIgnoreCaseAndTypeAndUserId(String name, CategoryType type, UUID userId);

    boolean existsByNameIgnoreCaseAndTypeAndUserIdAndIdNot(String name, CategoryType type, UUID userId, UUID id);
}
