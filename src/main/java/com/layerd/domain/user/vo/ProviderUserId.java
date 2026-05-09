package com.layerd.domain.user.vo;

import com.layerd.domain.user.exception.UserErrorCode;
import com.layerd.domain.user.exception.UserException;

public record ProviderUserId(String value) {

    public ProviderUserId {
        if (value == null || value.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_PROVIDER_USER_ID);
        }
    }

    public static ProviderUserId of(String value) {
        return new ProviderUserId(value == null ? null : value.trim());
    }
}
