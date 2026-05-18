package com.pium.domain.user.vo;

import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;

import java.util.UUID;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_USER_ID);
        }
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
