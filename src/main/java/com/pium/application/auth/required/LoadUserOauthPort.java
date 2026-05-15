package com.pium.application.auth.required;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.model.UserOauth;
import com.pium.domain.user.vo.ProviderUserId;

import java.util.Optional;

public interface LoadUserOauthPort {

    Optional<UserOauth> findByProviderAndProviderUserId(
            OauthProvider provider,
            ProviderUserId providerUserId
    );
}
