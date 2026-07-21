package com.pium.adapter.inbound.exception;

import com.pium.adapter.inbound.response.ApiResponse;
import com.pium.adapter.inbound.response.ErrorResult;
import com.pium.adapter.inbound.response.FieldValidationError;
import com.pium.exception.BaseException;
import com.pium.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return fail(errorCode, ErrorResult.of(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldValidationError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldValidationError)
                .toList();

        return fail(
                CommonErrorCode.VALIDATION_FAILED,
                ErrorResult.of(
                        CommonErrorCode.VALIDATION_FAILED.getCode(),
                        CommonErrorCode.VALIDATION_FAILED.getMessage(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        List<FieldValidationError> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> FieldValidationError.of(
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ))
                .toList();

        return fail(
                CommonErrorCode.VALIDATION_FAILED,
                ErrorResult.of(
                        CommonErrorCode.VALIDATION_FAILED.getCode(),
                        CommonErrorCode.VALIDATION_FAILED.getMessage(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            MultipartException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception exception) {
        log.debug("Malformed request", exception);
        return fail(
                CommonErrorCode.MALFORMED_REQUEST,
                ErrorResult.of(
                        CommonErrorCode.MALFORMED_REQUEST.getCode(),
                        CommonErrorCode.MALFORMED_REQUEST.getMessage()
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        log.debug("Method not allowed", exception);
        return fail(
                CommonErrorCode.METHOD_NOT_ALLOWED,
                ErrorResult.of(
                        CommonErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        CommonErrorCode.METHOD_NOT_ALLOWED.getMessage()
                )
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        log.debug("Unsupported media type", exception);
        return fail(
                CommonErrorCode.UNSUPPORTED_MEDIA_TYPE,
                ErrorResult.of(
                        CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                        CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return fail(
                CommonErrorCode.INTERNAL_SERVER_ERROR,
                ErrorResult.of(
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                )
        );
    }

    private FieldValidationError toFieldValidationError(FieldError error) {
        return FieldValidationError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
        );
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode, ErrorResult errorResult) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorResult));
    }
}
