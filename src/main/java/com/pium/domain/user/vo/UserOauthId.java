package com.pium.domain.user.vo;

import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;

import java.util.UUID;

public record UserOauthId(String value) {

    public UserOauthId {
        if (value == null || value.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_USER_OAUTH_ID);
        }
    }

    public static UserOauthId newId() {
        return new UserOauthId(UUID.randomUUID().toString());
    }

    public static UserOauthId of(String value) {
        return new UserOauthId(value);
    }
}
