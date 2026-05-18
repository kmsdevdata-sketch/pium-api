package com.pium.domain.skinanalysis.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SkinAnalysisErrorCode implements ErrorCode {

    INVALID_SKIN_ANALYSIS_RESULT_ID(HttpStatus.BAD_REQUEST, "SKIN_ANALYSIS_400_001", "유효하지 않은 피부 분석 결과 ID입니다."),
    SKIN_METRIC_REQUIRED(HttpStatus.BAD_REQUEST, "SKIN_ANALYSIS_400_003", "피부 상태 지표(metric)는 필수입니다."),
    SKIN_GOALS_REQUIRED(HttpStatus.BAD_REQUEST,"SKIN_ANALYSIS_400_004","피부 고민은 필수입니다."),
    INVALID_SKIN_METRIC_SCORE_RANGE(HttpStatus.BAD_REQUEST, "SKIN_ANALYSIS_400_005", "피부 상태 점수(score)는 0부터 100 사이여야 합니다."),
    SKIN_METRIC_SCORES_EMPTY(HttpStatus.BAD_REQUEST, "SKIN_ANALYSIS_400_007", "피부 상태 점수 목록은 비어 있을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
