package com.ruan.fincore.user.service;

import com.ruan.fincore.common.exception.BusinessException;
import com.ruan.fincore.common.exception.ResourceNotFoundException;
import com.ruan.fincore.user.dto.UserRequest;
import com.ruan.fincore.user.dto.UserResponse;
import com.ruan.fincore.user.entity.User;
import com.ruan.fincore.user.mapper.UserMapper;
import com.ruan.fincore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return UserMapper.toResponse(findByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UserRequest request) {
        User user = findByEmail(email);
        String newEmail = request.email().toLowerCase();
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new BusinessException("Email já cadastrado");
        }
        user.setName(request.name());
        user.setEmail(newEmail);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return UserMapper.toResponse(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
