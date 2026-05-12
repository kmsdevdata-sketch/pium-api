package com.pium.adapter.inbound.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResult(

        @Schema(description = "업무/도메인 에러 코드", example = "USER_404")
        String code,

        @Schema(description = "에러 메시지", example = "사용자를 찾을수 없습니다")
        String message,

        @ArraySchema(arraySchema = @Schema(description = "입력값 검증 실패 목록"))
        List<FieldValidationError> fieldErrors
) {
    public static ErrorResult of(String code , String message) {
        return new ErrorResult(code, message, null);
    }

    public static ErrorResult of(String code, String message, List<FieldValidationError> fieldErrors) {
        return new ErrorResult(code, message, fieldErrors);
    }

}
