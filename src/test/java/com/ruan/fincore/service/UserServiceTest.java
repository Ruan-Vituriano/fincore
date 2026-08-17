package com.ruan.fincore.service;

import com.ruan.fincore.dto.user.UserRequest;
import com.ruan.fincore.dto.user.UserResponse;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfileShouldReturnUserWhenExists() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("test@example.com");

        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    void getProfileShouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfileShouldUpdateNameAndPassword() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded-new");

        UserResponse response = userService.updateProfile("test@example.com",
                new UserRequest("New Name", "new@example.com", "newpass123"));

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void updateProfileShouldThrowBusinessExceptionWhenEmailAlreadyUsed() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("test@example.com",
                new UserRequest("New Name", "other@example.com", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email já cadastrado");
    }

    @Test
    void updateProfileShouldNotChangePasswordWhenBlank() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("test@example.com");
        user.setPassword("current");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.updateProfile("test@example.com", new UserRequest("New Name", "test@example.com", ""));

        assertThat(user.getPassword()).isEqualTo("current");
    }
}
