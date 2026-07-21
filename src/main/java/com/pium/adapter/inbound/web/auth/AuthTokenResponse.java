package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.AuthTokenView;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds
) {

    public static AuthTokenResponse from(AuthTokenView view) {
        return new AuthTokenResponse(
                view.tokenType(),
                view.accessToken(),
                view.refreshToken(),
                view.accessTokenExpiresInSeconds(),
                view.refreshTokenExpiresInSeconds()
        );
    }
}
