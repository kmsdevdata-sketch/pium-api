package com.pium.application.auth.required.dto;

public record KakaoAccessToken(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String scope
) {
}
