package com.pium.adapter.inbound.web.auth;

import com.pium.application.auth.dto.LoginCommand;
import com.pium.application.auth.dto.OauthClientType;
import com.pium.domain.user.enumtype.OauthProvider;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String provider,

        @NotBlank
        String authorizationCode,

        String referrer,
        String clientType
) {

    public LoginCommand toCommand() {
        return new LoginCommand(
                OauthProvider.of(provider),
                authorizationCode,
                referrer,
                OauthClientType.of(clientType)
        );
    }
}
