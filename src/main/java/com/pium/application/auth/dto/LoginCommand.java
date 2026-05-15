package com.pium.application.auth.dto;

import com.pium.domain.user.enumtype.OauthProvider;

public record LoginCommand(
        OauthProvider provider,
        String authorizationCode,
        String referrer
) {
}
