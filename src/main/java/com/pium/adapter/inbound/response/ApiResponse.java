package com.pium.adapter.inbound.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "성공 시 응답 데이터")
        T data,

        @Schema(description = "실패 시 에러 정보")
        ErrorResult error
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorResult error) {
        return new ApiResponse<>(false, null, error);
    }
}
