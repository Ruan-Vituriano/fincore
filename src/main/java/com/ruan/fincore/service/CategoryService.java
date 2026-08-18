package com.ruan.fincore.service;

import com.ruan.fincore.dto.category.CategoryRequest;
import com.ruan.fincore.dto.category.CategoryResponse;
import com.ruan.fincore.entity.Category;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.CategoryType;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.CategoryMapper;
import com.ruan.fincore.repository.CategoryRepository;
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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        boolean owned = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!owned) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        return category;
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
