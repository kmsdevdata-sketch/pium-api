package com.pium.adapter.outbound.survey.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SurveyAdapterErrorCode implements ErrorCode {

    SURVEY_SPEC_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY_ADAPTER_500_001", "정적 설문 스펙을 로드하는 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

