package com.pium.application.auth.required.dto;

public record GoogleAuthenticatedUser(
        String userKey,
        String name
) {
}
