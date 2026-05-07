package com.layerd.domain.user;

public enum OauthProvider {
    GOOGLE, KAKAO;

    public static OauthProvider of(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException();
        }

        return switch (provider.trim().toLowerCase()) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            default -> throw new IllegalArgumentException();
        };
    }
}
