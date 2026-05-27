package com.pium.application.auth.required;

import com.pium.application.auth.required.dto.GoogleAuthenticatedUser;

/**
 * Required Port
 * - Google 액세스 토큰으로 사용자 식별 정보를 조회하기 위한 포트
 */
public interface LoadGoogleUserPort {

    GoogleAuthenticatedUser load(String accessToken);
}
