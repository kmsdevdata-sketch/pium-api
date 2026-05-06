package com.layerd.domain.user;

import java.util.UUID;

public record UserId(String value) {

    public UserId{
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
