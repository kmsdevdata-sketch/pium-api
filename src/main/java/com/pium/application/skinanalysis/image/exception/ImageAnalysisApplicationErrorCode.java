package com.pium.application.skinanalysis.image.exception;

import com.pium.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageAnalysisApplicationErrorCode implements ErrorCode {

    INVALID_IMAGE_ANALYZE_COMMAND(HttpStatus.BAD_REQUEST, "IMAGE_ANALYSIS_APP_400_001", "사진 분석 요청이 올바르지 않습니다."),
    IMAGE_UNANALYZABLE(HttpStatus.BAD_REQUEST, "IMAGE_ANALYSIS_APP_400_002", "사진을 분석할 수 없습니다. 밝은 곳에서 얼굴이 잘 보이게 다시 촬영해 주세요."),
    IMAGE_ANALYSIS_SESSION_EXPIRED(HttpStatus.GONE, "IMAGE_ANALYSIS_APP_410_001", "사진 분석 세션이 만료되었습니다. 다시 촬영해 주세요."),
    IMAGE_ANALYSIS_SESSION_FORBIDDEN(HttpStatus.FORBIDDEN, "IMAGE_ANALYSIS_APP_403_001", "사진 분석 세션에 접근할 수 없습니다."),
    IMAGE_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_ANALYSIS_APP_500_001", "사진 기반 피부 분석에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
