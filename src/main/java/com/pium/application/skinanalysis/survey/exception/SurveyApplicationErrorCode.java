package com.pium.application.skinanalysis.survey.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SurveyApplicationErrorCode implements ErrorCode {

    SURVEY_SPEC_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY_APP_500_001", "설문 스펙을 조회할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

