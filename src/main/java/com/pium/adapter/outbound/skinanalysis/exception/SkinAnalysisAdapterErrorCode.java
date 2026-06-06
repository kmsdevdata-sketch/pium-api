package com.pium.adapter.outbound.skinanalysis.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SkinAnalysisAdapterErrorCode implements ErrorCode {

    INVALID_ANALYZE_COMMAND(HttpStatus.BAD_REQUEST, "SKIN_ADAPTER_400_001", "분석 요청 본문이 올바르지 않습니다."),
    INVALID_ANSWER_PAYLOAD(HttpStatus.BAD_REQUEST, "SKIN_ADAPTER_400_002", "문항 응답 형식이 올바르지 않습니다."),
    INVALID_OPTION_CODES(HttpStatus.BAD_REQUEST, "SKIN_ADAPTER_400_003", "선택 옵션 코드가 비어있거나 유효하지 않습니다."),
    INVALID_IMAGE_PAYLOAD(HttpStatus.BAD_REQUEST, "SKIN_ADAPTER_400_004", "사진 분석 이미지 형식이 올바르지 않습니다."),

    SKIN_ANALYSIS_RESULT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SKIN_ADAPTER_500_001", "피부 분석 결과 저장 중 오류가 발생했습니다."),
    SKIN_METRIC_SCORE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SKIN_ADAPTER_500_002", "피부 지표 점수 저장 중 오류가 발생했습니다."),
    IMAGE_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SKIN_ADAPTER_500_003", "사진 기반 피부 분석에 실패했습니다."),
    IMAGE_ANALYSIS_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "SKIN_ADAPTER_500_004", "사진 기반 피부 분석 응답이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
