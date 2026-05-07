package com.layerd.domain.user.fixture;

import com.layerd.domain.user.OauthProvider;
import com.layerd.domain.user.ProviderUserId;
import com.layerd.domain.user.User;
import com.layerd.domain.user.UserOauth;

public class UserOauthFixture {

    public static UserOauth createUserOauth(User user) {
        return UserOauth.create(
                user.getId(),
                OauthProvider.GOOGLE,
                ProviderUserId.of("abcd1234")
        );
    }
}