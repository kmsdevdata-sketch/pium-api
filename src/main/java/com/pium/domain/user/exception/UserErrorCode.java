package com.pium.domain.user.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "USER_400_001", "유효하지 않은 사용자 ID입니다."),
    INVALID_USER_OAUTH_ID(HttpStatus.BAD_REQUEST, "USER_400_002", "유효하지 않은 사용자 OAuth ID입니다."),
    INVALID_USER_PROFILE_ID(HttpStatus.BAD_REQUEST, "USER_400_003", "유효하지 않은 사용자 프로필 ID입니다."),
    INVALID_PROVIDER_USER_ID(HttpStatus.BAD_REQUEST, "USER_400_004", "유효하지 않은 OAuth Provider 사용자 ID입니다."),
    INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "USER_400_005", "OAuth Provider 값이 비어있거나 유효하지 않습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "USER_400_006", "지원하지 않는 OAuth Provider입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "USER_400_007", "유효하지 않은 닉네임입니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "USER_403_001", "탈퇴 혹은 이용불가 사용자입니다."),

    ALREADY_BANNED_USER(HttpStatus.CONFLICT, "USER_409_002", "이미 차단된 사용자입니다."),
    ALREADY_WITHDRAWN_USER(HttpStatus.CONFLICT, "USER_409_001", "이미 탈퇴한 사용자입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
