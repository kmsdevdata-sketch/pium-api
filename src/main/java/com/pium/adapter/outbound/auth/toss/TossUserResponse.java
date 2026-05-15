package com.pium.adapter.outbound.auth.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossUserResponse(
        String resultType,
        Success success,
        Error error
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Success(
            Long userKey,
            String scope,
            List<String> agreedTerms,
            String name,
            String phone,
            String birthday,
            String ci,
            String di,
            String gender,
            String nationality,
            String email
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(
            String errorCode,
            String reason
    ) {
    }
}