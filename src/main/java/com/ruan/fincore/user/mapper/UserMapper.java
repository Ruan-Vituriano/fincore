package com.ruan.fincore.user.mapper;

import com.ruan.fincore.user.dto.UserResponse;
import com.ruan.fincore.user.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
