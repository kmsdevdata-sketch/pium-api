package com.pium.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pium.adapter.inbound.exception.CommonErrorCode;
import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.adapter.inbound.response.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        CommonErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;
        write(response, errorCode);
    }

    private void write(HttpServletResponse response, CommonErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(
                response.getWriter(),
                ApiResponse.fail(ErrorResult.of(errorCode.getCode(), errorCode.getMessage()))
        );
    }
}
