package com.layerd.domain.user;

public class UserOauthFixture {

    public static UserOauth createUserOauth(User user) {
        return UserOauth.create(
                user.getId(),
                OauthProvider.GOOGLE,
                ProviderUserId.of("abcd1234")
        );
    }
}