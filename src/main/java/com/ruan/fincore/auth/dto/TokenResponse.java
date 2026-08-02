package com.ruan.fincore.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType
) {
}
