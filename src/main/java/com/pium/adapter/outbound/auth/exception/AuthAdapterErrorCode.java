package com.pium.adapter.outbound.auth.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthAdapterErrorCode implements ErrorCode {

    TOSS_TOKEN_EXCHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ADAPTER_500_001", "토스 액세스 토큰 교환에 실패했습니다."),
    TOSS_USER_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ADAPTER_500_002", "토스에서 사용자 정보를 조회하는데 실패했습니다."),
    GOOGLE_TOKEN_EXCHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ADAPTER_500_003", "구글 액세스 토큰 교환에 실패했습니다."),
    GOOGLE_USER_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ADAPTER_500_004", "구글에서 사용자 정보를 조회하는데 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
