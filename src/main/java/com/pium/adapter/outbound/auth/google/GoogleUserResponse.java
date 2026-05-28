package com.pium.adapter.outbound.auth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserResponse(
        String sub,
        String email,

        @JsonProperty("email_verified")
        Boolean emailVerified,

        String name,
        String picture
) {
}
