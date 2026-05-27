package com.pium.application.auth.required.dto;

public record GoogleAccessToken(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String idToken
) {
}
