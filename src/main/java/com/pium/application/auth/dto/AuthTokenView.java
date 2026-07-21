package com.pium.application.auth.dto;

public record AuthTokenView(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds
) {
}
