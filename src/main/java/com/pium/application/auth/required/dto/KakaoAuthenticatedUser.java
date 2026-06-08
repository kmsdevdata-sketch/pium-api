package com.pium.application.auth.required.dto;

public record KakaoAuthenticatedUser(
        String userKey,
        String name
) {
}
