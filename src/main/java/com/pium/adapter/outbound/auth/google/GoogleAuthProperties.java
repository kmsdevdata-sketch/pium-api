package com.pium.adapter.outbound.auth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Google OAuth 인증 관련 설정 객체
 */
@ConfigurationProperties(prefix = "auth.google")
public record GoogleAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenUri,
        String userInfoUri
) {
}
