package com.pium.adapter.outbound.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationSeconds
) {
}
