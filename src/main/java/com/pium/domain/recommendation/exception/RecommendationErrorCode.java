package com.pium.domain.recommendation.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

    INVALID_SKIN_INTERPRETATION(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_001", "유효하지 않은 피부 해석 결과입니다."),
    INVALID_SKIN_NEED(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_002", "유효하지 않은 피부 니즈입니다."),
    INVALID_RISK_CONSTRAINT(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_003", "유효하지 않은 추천 risk 제약입니다."),
    INVALID_GOAL_NEED(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_004", "유효하지 않은 추천 goal 니즈입니다."),
    INVALID_GOAL_CONFLICT_NOTICE(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_005", "유효하지 않은 goal 충돌 안내입니다."),
    INVALID_SKIN_METRIC_LEVELS(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_006", "유효하지 않은 피부 지표 레벨입니다."),
    INVALID_RECOMMENDATION_POLICY(HttpStatus.BAD_REQUEST, "RECOMMENDATION_400_007", "유효하지 않은 추천 정책 입력입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
