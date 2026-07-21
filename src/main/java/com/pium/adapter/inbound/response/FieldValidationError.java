package com.pium.adapter.inbound.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldValidationError(

        @Schema(description = "검증 실패 필드명", example = "nickname")
        String field,

        @Schema(description = "잘못 들어온 값 ", example = "")
        Object rejectedValue,

        @Schema(description = "검증 실패 사유", example = "닉네임은 필수입니다")
        String reason
) {
    public static FieldValidationError of(String field, Object rejectedValue, String reason) {
        return new FieldValidationError(field, rejectedValue, reason);
    }
}
