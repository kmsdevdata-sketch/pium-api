package com.pium.application.skinanalysis.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SurveyApplicationErrorCode implements ErrorCode {

    CURRENT_USER_UNAVAILABLE(HttpStatus.UNAUTHORIZED, "SURVEY_APP_401_001", "현재 사용자 식별 정보를 확인할 수 없습니다."),
    SURVEY_SPEC_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY_APP_500_001", "설문 스펙을 조회할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
