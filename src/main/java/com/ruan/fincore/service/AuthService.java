package com.ruan.fincore.service;

import com.ruan.fincore.dto.auth.LoginRequest;
import com.ruan.fincore.dto.auth.RegisterRequest;
import com.ruan.fincore.dto.auth.TokenResponse;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return new TokenResponse(jwtService.generateToken(user), "Bearer");
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas"));
        return new TokenResponse(jwtService.generateToken(user), "Bearer");
    }
}
