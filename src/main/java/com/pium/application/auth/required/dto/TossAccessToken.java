package com.pium.application.auth.required.dto;

public record TossAccessToken(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
