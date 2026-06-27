package com.acorn.elearning.common.response;

import java.util.List;

public record ApiResponse<T>(boolean success, String message, T data, ApiError error) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 정상적으로 처리되었습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> fail(String message, String code, String detail) {
        return fail(message, code, detail, List.of());
    }

    public static <T> ApiResponse<T> fail(String message, String code, String detail, List<FieldError> fieldErrors) {
        return new ApiResponse<>(false, message, null, new ApiError(code, detail, fieldErrors));
    }

    public record ApiError(String code, String detail, List<FieldError> fieldErrors) {
        public ApiError {
            fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
        }
    }

    public record FieldError(String field, String message) {}
}
