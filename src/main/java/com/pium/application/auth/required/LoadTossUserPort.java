package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.TossAuthenticatedUser;

/**
 * Required Port
 * - 토스 액세스 토큰으로 토스 로그인 사용자 정보를 조회하는 역할
 */
public interface LoadTossUserPort {

    TossAuthenticatedUser load(String accessToken);
}
