package com.pium.domain.productprofile.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductProfileErrorCode implements ErrorCode {

    INVALID_PRODUCT_PROFILE(HttpStatus.BAD_REQUEST, "PRODUCT_PROFILE_400_001", "유효하지 않은 상품 프로파일입니다."),
    INVALID_TRAIT_SIGNAL(HttpStatus.BAD_REQUEST, "PRODUCT_PROFILE_400_002", "유효하지 않은 상품 trait 신호입니다."),
    INVALID_EVIDENCE_SIGNAL(HttpStatus.BAD_REQUEST, "PRODUCT_PROFILE_400_003", "유효하지 않은 상품 프로파일 근거입니다."),
    INVALID_EVIDENCE_REFERENCE(HttpStatus.BAD_REQUEST, "PRODUCT_PROFILE_400_004", "상품 trait의 근거 참조가 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
