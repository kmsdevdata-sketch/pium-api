package com.pium.adapter.outbound.auth.google;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.LoadGoogleUserPort;
import com.pium.application.auth.required.dto.GoogleAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleUserLoadAdapter implements LoadGoogleUserPort {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient.Builder restClientBuilder;
    private final GoogleAuthProperties googleAuthProperties;

    @Override
    public GoogleAuthenticatedUser load(String accessToken) {
        GoogleUserResponse response;

        try {
            response = restClientBuilder.build()
                    .get()
                    .uri(googleAuthProperties.userInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                    .retrieve()
                    .body(GoogleUserResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "Google user load HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.GOOGLE_USER_LOAD_FAILED);
        }

        if (response == null || response.sub() == null || response.sub().isBlank()) {
            throw new AuthAdapterException(AuthAdapterErrorCode.GOOGLE_USER_LOAD_FAILED);
        }

        return new GoogleAuthenticatedUser(
                response.sub(),
                resolveName(response)
        );
    }

    private String resolveName(GoogleUserResponse response) {
        if (response.name() != null && !response.name().isBlank()) {
            return response.name().trim();
        }
        if (response.email() != null && !response.email().isBlank()) {
            return response.email().trim();
        }
        return null;
    }
}
