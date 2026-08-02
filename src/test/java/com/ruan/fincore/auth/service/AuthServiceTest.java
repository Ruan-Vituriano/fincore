package com.ruan.fincore.auth.service;

import com.ruan.fincore.auth.dto.LoginRequest;
import com.ruan.fincore.auth.dto.RegisterRequest;
import com.ruan.fincore.auth.dto.TokenResponse;
import com.ruan.fincore.common.exception.BusinessException;
import com.ruan.fincore.user.entity.User;
import com.ruan.fincore.user.enums.Role;
import com.ruan.fincore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldReturnTokenWhenEmailIsAvailable() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        TokenResponse response = authService.register(new RegisterRequest("Test User", "test@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowBusinessExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Test User", "test@example.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email já cadastrado");
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        TokenResponse response = authService.login(new LoginRequest("test@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any());
    }
}
