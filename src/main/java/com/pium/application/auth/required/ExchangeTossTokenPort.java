package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.TossAccessToken;

/**
 * Required Port
 * - 토스 로그인 인가 코드를 토스 액세스 토큰으로 교환하는 역할
 */
public interface ExchangeTossTokenPort {

    TossAccessToken exchange(String authorizationCode, String referrer);
}
