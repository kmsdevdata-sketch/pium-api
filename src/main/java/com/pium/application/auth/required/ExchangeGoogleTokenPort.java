package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.GoogleAccessToken;

/**
 * Required Port
 * - Google authorization code를 액세스 토큰으로 교환하기 위한 포트
 */
public interface ExchangeGoogleTokenPort {

    GoogleAccessToken exchange(String authorizationCode);
}
