package com.pium.application.auth.dto;

import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;

public enum OauthClientType {
    ADMIN,
    WEB;

    public static OauthClientType of(String value) {
        if (value == null || value.isBlank()) {
            return ADMIN;
        }
        try {
            return OauthClientType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UserException(UserErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
    }
}
