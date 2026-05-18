package com.pium.adapter.outbound.auth.toss;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.ExchangeTossTokenPort;
import com.pium.application.auth.required.dto.TossAccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossTokenExchangeAdapter implements ExchangeTossTokenPort {

    @Qualifier("tossRestClient")
    private final RestClient tossRestClient;
    private final TossAuthProperties tossAuthProperties;

    @Override
    public TossAccessToken exchange(String authorizationCode, String referrer) {
        TossTokenResponse response;

        try {
            response = tossRestClient
                    .post()
                    .uri(tossAuthProperties.baseUrl() + "/api-partner/v1/apps-in-toss/user/oauth2/generate-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "authorizationCode", authorizationCode,
                                    "referrer", referrer
                            )
                    )
                    .retrieve()
                    .body(TossTokenResponse.class);
        } catch (RestClientResponseException e) {
            log.warn(
                    "Toss token exchange HTTP error. status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.TOSS_TOKEN_EXCHANGE_FAILED);
        }

        if (response == null || response.success() == null) {
            TossTokenResponse.Error error = response == null ? null : response.error();
            log.warn(
                    "Toss token exchange failed. resultType={}, errorCode={}, reason={}, referrer={}, authorizationCodePresent={}",
                    response == null ? null : response.resultType(),
                    error == null ? null : error.errorCode(),
                    error == null ? null : error.reason(),
                    referrer,
                    authorizationCode != null && !authorizationCode.isBlank()
            );
            throw new AuthAdapterException(AuthAdapterErrorCode.TOSS_TOKEN_EXCHANGE_FAILED);
        }

        TossTokenResponse.Success success = response.success();

        return new TossAccessToken(
                success.accessToken(),
                success.refreshToken(),
                success.tokenType(),
                success.expiresIn()
        );
    }
}
