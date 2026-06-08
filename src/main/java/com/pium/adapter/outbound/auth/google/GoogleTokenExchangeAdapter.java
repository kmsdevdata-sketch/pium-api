package com.pium.adapter.outbound.auth.google;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.dto.OauthClientType;
import com.pium.application.auth.required.ExchangeGoogleTokenPort;
import com.pium.application.auth.required.dto.GoogleAccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenExchangeAdapter implements ExchangeGoogleTokenPort {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final RestClient.Builder restClientBuilder;
    private final GoogleAuthProperties googleAuthProperties;

    @Override
    public GoogleAccessToken exchange(String authorizationCode, OauthClientType clientType) {
        GoogleTokenResponse response;

        try {
            response = restClientBuilder.build()
                    .post()
                    .uri(googleAuthProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(tokenRequestBody(authorizationCode, clientType))
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "Google token exchange HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.GOOGLE_TOKEN_EXCHANGE_FAILED);
        }

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new AuthAdapterException(AuthAdapterErrorCode.GOOGLE_TOKEN_EXCHANGE_FAILED);
        }

        return new GoogleAccessToken(
                response.accessToken(),
                response.refreshToken(),
                response.tokenType(),
                response.expiresIn() == null ? 0L : response.expiresIn(),
                response.idToken()
        );
    }

    private MultiValueMap<String, String> tokenRequestBody(String authorizationCode, OauthClientType clientType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", googleAuthProperties.clientId());
        body.add("client_secret", googleAuthProperties.clientSecret());
        body.add("redirect_uri", redirectUri(clientType));
        body.add("grant_type", AUTHORIZATION_CODE);
        return body;
    }

    private String redirectUri(OauthClientType clientType) {
        if (clientType == OauthClientType.WEB) {
            return googleAuthProperties.webRedirectUri();
        }
        return googleAuthProperties.redirectUri();
    }
}
