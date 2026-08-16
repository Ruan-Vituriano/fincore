package com.ruan.fincore.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
) {
}
