package com.layerd.domain.user.vo;

import com.layerd.domain.user.exception.UserErrorCode;
import com.layerd.domain.user.exception.UserException;

import java.util.UUID;

public record UserProfileId(String value) {

    public UserProfileId {
        if (value == null || value.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_USER_PROFILE_ID);
        }
    }

    public static UserProfileId newId() {
        return new UserProfileId(UUID.randomUUID().toString());
    }

    public static UserProfileId of(String value) {
        return new UserProfileId(value == null ? null : value.trim());
    }
}
