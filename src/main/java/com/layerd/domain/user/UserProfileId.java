package com.layerd.domain.user;

import java.util.UUID;

public record UserProfileId(String value) {

    public UserProfileId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("userProfileId must not be blank");
        }
    }

    public static UserProfileId newId() {
        return new UserProfileId(UUID.randomUUID().toString());
    }

    public static UserProfileId of(String value) {
        return new UserProfileId(value == null ? null : value.trim());
    }
}
