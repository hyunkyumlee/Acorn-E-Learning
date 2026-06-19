package com.acorn.elearning.common.exception;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity.status(errorCode.status()).body(ApiResponse.fail(exception.getMessage(), errorCode.code(), exception.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        List<ApiResponse.FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();
        ErrorCode errorCode = ErrorCode.COMMON_VALIDATION_FAILED;
        return ResponseEntity.status(errorCode.status()).body(ApiResponse.fail(errorCode.message(), errorCode.code(), "Bean Validation failed", fieldErrors));
    }
    private ApiResponse.FieldError toFieldError(FieldError error) { return new ApiResponse.FieldError(error.getField(), error.getDefaultMessage()); }
}
