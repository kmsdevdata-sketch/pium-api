package com.pium.adapter.outbound.auth.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthAdapterErrorCode implements ErrorCode {

    TOSS_TOKEN_EXCHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ADAPTER_500_001", "토스 액세스 토큰 교환에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
