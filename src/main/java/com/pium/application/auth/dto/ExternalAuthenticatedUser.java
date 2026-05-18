package com.pium.application.auth.dto;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.vo.ProviderUserId;

public record ExternalAuthenticatedUser(
        OauthProvider provider,
        ProviderUserId providerUserId,
        String name
) {
}
