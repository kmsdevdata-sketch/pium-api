package com.pium.application.recommendation.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationApplicationErrorCode implements ErrorCode {

    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_APP_404_001", "추천 결과를 조회할 수 없습니다."),
    RECOMMENDED_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_APP_404_002", "추천 상품을 조회할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
