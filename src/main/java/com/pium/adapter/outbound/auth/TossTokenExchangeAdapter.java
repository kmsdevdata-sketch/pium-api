package com.pium.adapter.outbound.auth;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.ExchangeTossTokenPort;
import com.pium.application.auth.required.dto.TossAccessToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossTokenExchangeAdapter implements ExchangeTossTokenPort {

    private final RestClient.Builder restClientBuilder;
    private final TossAuthProperties tossAuthProperties;

    @Override
    public TossAccessToken exchange(String authorizationCode, String referrer) {
        TossTokenResponse response = restClientBuilder.build()
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

        if (response == null || response.success() == null) {
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
