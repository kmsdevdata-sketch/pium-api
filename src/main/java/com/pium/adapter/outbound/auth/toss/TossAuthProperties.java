package com.pium.adapter.outbound.auth.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 토스 인증 관련 설정 객체
 */
@ConfigurationProperties(prefix = "auth.toss")
public record TossAuthProperties(
        String baseUrl,
        String decryptKey,
        String aad
) {
}
