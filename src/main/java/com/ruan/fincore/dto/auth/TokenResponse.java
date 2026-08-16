package com.ruan.fincore.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType
) {
}
