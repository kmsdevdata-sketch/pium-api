package com.pium.adapter.outbound.auth.kakao;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.ExchangeKakaoTokenPort;
import com.pium.application.auth.required.dto.KakaoAccessToken;
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
public class KakaoTokenExchangeAdapter implements ExchangeKakaoTokenPort {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final RestClient.Builder restClientBuilder;
    private final KakaoAuthProperties kakaoAuthProperties;

    @Override
    public KakaoAccessToken exchange(String authorizationCode) {
        KakaoTokenResponse response;

        try {
            response = restClientBuilder.build()
                    .post()
                    .uri(kakaoAuthProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(tokenRequestBody(authorizationCode))
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "Kakao token exchange HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        }

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new AuthAdapterException(AuthAdapterErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        }

        return new KakaoAccessToken(
                response.accessToken(),
                response.refreshToken(),
                response.tokenType(),
                response.expiresIn() == null ? 0L : response.expiresIn(),
                response.scope()
        );
    }

    private MultiValueMap<String, String> tokenRequestBody(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", AUTHORIZATION_CODE);
        body.add("client_id", kakaoAuthProperties.restApiKey());
        body.add("redirect_uri", kakaoAuthProperties.redirectUri());
        body.add("code", authorizationCode);
        if (kakaoAuthProperties.clientSecret() != null && !kakaoAuthProperties.clientSecret().isBlank()) {
            body.add("client_secret", kakaoAuthProperties.clientSecret());
        }
        return body;
    }
}
