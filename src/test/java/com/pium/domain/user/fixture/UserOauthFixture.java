package com.pium.domain.user.fixture;

import com.pium.domain.user.enumtype.OauthProvider;
import com.pium.domain.user.vo.ProviderUserId;
import com.pium.domain.user.model.User;
import com.pium.domain.user.model.UserOauth;

public class UserOauthFixture {

    public static UserOauth createUserOauth(User user) {
        return UserOauth.create(
                user.getId(),
                OauthProvider.GOOGLE,
                ProviderUserId.of("abcd1234")
        );
    }
}