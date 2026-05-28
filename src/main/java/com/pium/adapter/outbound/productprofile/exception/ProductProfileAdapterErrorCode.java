package com.pium.adapter.outbound.productprofile.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductProfileAdapterErrorCode implements ErrorCode {

    PRODUCT_PROFILE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_PROFILE_ADAPTER_500_001", "상품 프로파일 생성에 실패했습니다."),
    PRODUCT_PROFILE_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_PROFILE_ADAPTER_500_002", "상품 프로파일 생성 응답이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
