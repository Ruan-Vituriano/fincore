package com.ruan.fincore.category.service;

import com.ruan.fincore.category.dto.CategoryRequest;
import com.ruan.fincore.category.dto.CategoryResponse;
import com.ruan.fincore.category.entity.Category;
import com.ruan.fincore.category.enums.CategoryType;
import com.ruan.fincore.category.mapper.CategoryMapper;
import com.ruan.fincore.category.repository.CategoryRepository;
import com.ruan.fincore.common.exception.BusinessException;
import com.ruan.fincore.common.exception.ResourceNotFoundException;
import com.ruan.fincore.user.entity.User;
import com.ruan.fincore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(String email) {
        UUID userId = findUserId(email);
        return categoryRepository.findByUserIdOrUserIdIsNullOrderByName(userId).stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(String email, CategoryRequest request) {
        User user = userService.findByEmail(email);
        validateUniqueName(user.getId(), request.name(), request.type(), null);
        Category category = new Category();
        applyRequest(category, request);
        category.setUser(user);
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(String email, UUID id, CategoryRequest request) {
        UUID userId = findUserId(email);
        validateUniqueName(userId, request.name(), request.type(), id);
        Category category = findOwnedCategory(id, userId);
        applyRequest(category, request);
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(String email, UUID id) {
        Category category = findOwnedCategory(id, findUserId(email));
        categoryRepository.delete(category);
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private Category findOwnedCategory(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    private void validateUniqueName(UUID userId, String name, CategoryType type, UUID id) {
        boolean exists = id == null
                ? categoryRepository.existsByNameIgnoreCaseAndTypeAndUserId(name, type, userId)
                : categoryRepository.existsByNameIgnoreCaseAndTypeAndUserIdAndIdNot(name, type, userId, id);
        if (exists) {
            throw new BusinessException("Já existe uma categoria com este nome e tipo");
        }
    }

    private void applyRequest(Category category, CategoryRequest request) {
        category.setName(request.name());
        category.setType(request.type());
        category.setIcon(blankToNull(request.icon()));
        category.setColor(blankToNull(request.color()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
