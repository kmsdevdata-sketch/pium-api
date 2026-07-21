package com.pium.application.auth.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthApplicationErrorCode implements ErrorCode {

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_APP_401_001", "refresh token이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
