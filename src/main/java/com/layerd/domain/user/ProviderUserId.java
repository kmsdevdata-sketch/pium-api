package com.layerd.domain.user;

public record ProviderUserId(String value) {

    public ProviderUserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public static ProviderUserId of(String value) {
        return new ProviderUserId(value.trim());
    }
}
