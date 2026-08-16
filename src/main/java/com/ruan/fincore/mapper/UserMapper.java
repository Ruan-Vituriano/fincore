package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.user.UserResponse;
import com.ruan.fincore.entity.User;

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
