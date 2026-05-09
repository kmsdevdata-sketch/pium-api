package com.layerd.domain.user.enumtype;

import com.layerd.domain.user.exception.UserErrorCode;
import com.layerd.domain.user.exception.UserException;

public enum OauthProvider {
    GOOGLE,
    KAKAO;

    public static OauthProvider of(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_OAUTH_PROVIDER);
        }

        return switch (provider.trim().toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            default -> throw new UserException(UserErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        };
    }
}
