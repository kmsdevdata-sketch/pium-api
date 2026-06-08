package com.pium.adapter.outbound.auth.kakao;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.LoadKakaoUserPort;
import com.pium.application.auth.required.dto.KakaoAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoUserLoadAdapter implements LoadKakaoUserPort {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient.Builder restClientBuilder;
    private final KakaoAuthProperties kakaoAuthProperties;

    @Override
    public KakaoAuthenticatedUser load(String accessToken) {
        KakaoUserResponse response;

        try {
            response = restClientBuilder.build()
                    .get()
                    .uri(kakaoAuthProperties.userInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "Kakao user load HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.KAKAO_USER_LOAD_FAILED);
        }

        if (response == null || response.id() == null) {
            throw new AuthAdapterException(AuthAdapterErrorCode.KAKAO_USER_LOAD_FAILED);
        }

        return new KakaoAuthenticatedUser(
                String.valueOf(response.id()),
                resolveNickname(response)
        );
    }

    private String resolveNickname(KakaoUserResponse response) {
        if (
                response.kakaoAccount() == null ||
                response.kakaoAccount().profile() == null ||
                response.kakaoAccount().profile().nickname() == null ||
                response.kakaoAccount().profile().nickname().isBlank()
        ) {
            return null;
        }
        return response.kakaoAccount().profile().nickname().trim();
    }
}
