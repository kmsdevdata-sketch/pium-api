package com.pium.domain.product.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "PRODUCT_400_001", "유효하지 않은 상품 ID입니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "PRODUCT_400_002", "유효하지 않은 상품명입니다."),
    INVALID_BRAND_NAME(HttpStatus.BAD_REQUEST, "PRODUCT_400_003", "유효하지 않은 브랜드명입니다."),
    INVALID_SOURCE_URL(HttpStatus.BAD_REQUEST, "PRODUCT_400_004", "유효하지 않은 상품 원본 링크입니다."),
    INVALID_PRODUCT_CATEGORY(HttpStatus.BAD_REQUEST, "PRODUCT_400_005", "유효하지 않은 상품 카테고리입니다."),
    INVALID_USAGE_STEP(HttpStatus.BAD_REQUEST, "PRODUCT_400_006", "유효하지 않은 상품 사용 단계입니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PRODUCT_400_007", "유효하지 않은 상품 상태입니다."),
    INVALID_FUNCTIONAL_LABEL(HttpStatus.BAD_REQUEST, "PRODUCT_400_008", "유효하지 않은 기능성 표시입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
