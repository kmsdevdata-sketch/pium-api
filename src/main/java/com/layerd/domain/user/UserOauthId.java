package com.layerd.domain.user;

import java.util.UUID;

public record UserOauthId(String value) {

    public UserOauthId{
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public static UserOauthId newId() {
        return new UserOauthId(UUID.randomUUID().toString());
    }

    public static UserOauthId of(String value) {
        return new UserOauthId(value);
    }
}
