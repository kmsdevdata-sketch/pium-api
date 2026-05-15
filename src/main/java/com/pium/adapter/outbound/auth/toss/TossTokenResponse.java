package com.pium.adapter.outbound.auth.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossTokenResponse(
        String resultType,
        Success success,
        Error error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Success(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String scope
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(
            String errorCode,
            String reason
    ) {
    }
}
