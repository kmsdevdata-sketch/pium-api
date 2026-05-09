package com.layerd.domain.user.fixture;

import com.layerd.domain.user.enumtype.OauthProvider;
import com.layerd.domain.user.vo.ProviderUserId;
import com.layerd.domain.user.model.User;
import com.layerd.domain.user.model.UserOauth;

public class UserOauthFixture {

    public static UserOauth createUserOauth(User user) {
        return UserOauth.create(
                user.getId(),
                OauthProvider.GOOGLE,
                ProviderUserId.of("abcd1234")
        );
    }
}