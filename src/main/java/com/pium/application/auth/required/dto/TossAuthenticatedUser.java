package com.pium.application.auth.required.dto;

public record TossAuthenticatedUser(
        String userKey,
        String name
) {
}
