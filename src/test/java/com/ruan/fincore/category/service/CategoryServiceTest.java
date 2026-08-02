package com.ruan.fincore.category.service;

import com.ruan.fincore.category.dto.CategoryRequest;
import com.ruan.fincore.category.dto.CategoryResponse;
import com.ruan.fincore.category.entity.Category;
import com.ruan.fincore.category.enums.CategoryType;
import com.ruan.fincore.category.repository.CategoryRepository;
import com.ruan.fincore.common.exception.BusinessException;
import com.ruan.fincore.common.exception.ResourceNotFoundException;
import com.ruan.fincore.user.entity.User;
import com.ruan.fincore.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
    }

    @Test
    void listShouldReturnGlobalAndUserCategories() {
        Category global = category("Salário", CategoryType.INCOME, null);
        Category own = category("Pessoal", CategoryType.EXPENSE, user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByUserIdOrUserIdIsNullOrderByName(USER_ID)).thenReturn(List.of(global, own));

        List<CategoryResponse> response = categoryService.list("test@example.com");

        assertThat(response).hasSize(2);
        assertThat(response.get(0).userId()).isNull();
        assertThat(response.get(1).userId()).isEqualTo(USER_ID);
    }

    @Test
    void createShouldReturnCreatedCategory() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndUserId("Lazer", CategoryType.EXPENSE, USER_ID))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.create("test@example.com",
                new CategoryRequest("Lazer", CategoryType.EXPENSE, "sports", "#FF0000"));

        assertThat(response.name()).isEqualTo("Lazer");
        assertThat(response.type()).isEqualTo(CategoryType.EXPENSE);
        assertThat(response.userId()).isEqualTo(USER_ID);
    }

    @Test
    void createShouldThrowBusinessExceptionWhenNameAndTypeAlreadyUsed() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndUserId("Lazer", CategoryType.EXPENSE, USER_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("test@example.com",
                new CategoryRequest("Lazer", CategoryType.EXPENSE, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma categoria com este nome e tipo");
    }

    @Test
    void updateShouldUpdateOwnedCategory() {
        Category category = category("Antiga", CategoryType.INCOME, user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndUserIdAndIdNot(
                "Nova", CategoryType.INCOME, USER_ID, category.getId())).thenReturn(false);

        CategoryResponse response = categoryService.update("test@example.com", category.getId(),
                new CategoryRequest("Nova", CategoryType.INCOME, null, null));

        assertThat(response.name()).isEqualTo("Nova");
        assertThat(category.getName()).isEqualTo("Nova");
    }

    @Test
    void updateShouldThrowNotFoundWhenCategoryIsNotOwned() {
        UUID otherId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(otherId, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update("test@example.com", otherId,
                new CategoryRequest("Nova", CategoryType.INCOME, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldDeleteOwnedCategory() {
        Category category = category("Lazer", CategoryType.EXPENSE, user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(category.getId(), USER_ID)).thenReturn(Optional.of(category));

        categoryService.delete("test@example.com", category.getId());

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteShouldThrowNotFoundWhenCategoryIsNotOwned() {
        UUID otherId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(categoryRepository.findByIdAndUserId(otherId, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete("test@example.com", otherId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Category category(String name, CategoryType type, User owner) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName(name);
        category.setType(type);
        category.setUser(owner);
        return category;
    }
}
