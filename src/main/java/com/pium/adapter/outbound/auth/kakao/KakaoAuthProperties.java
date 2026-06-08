package com.pium.adapter.outbound.auth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kakao OAuth authentication settings.
 */
@ConfigurationProperties(prefix = "auth.kakao")
public record KakaoAuthProperties(
        String restApiKey,
        String clientSecret,
        String redirectUri,
        String tokenUri,
        String userInfoUri
) {
}
