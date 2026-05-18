package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.LoginCommand;
import com.pium.domain.user.enumtype.OauthProvider;

public record LoginRequest(
        String provider,
        String authorizationCode,
        String referrer
) {

    public LoginCommand toCommand() {
        return new LoginCommand(
                OauthProvider.of(provider),
                authorizationCode,
                referrer
        );
    }
}
