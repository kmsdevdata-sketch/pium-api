package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.AuthTokenView;

public record AuthTokenResponse(
        String tokenType,
        String accessToken
) {

    public static AuthTokenResponse from(AuthTokenView view) {
        return new AuthTokenResponse(view.tokenType(), view.accessToken());
    }
}
