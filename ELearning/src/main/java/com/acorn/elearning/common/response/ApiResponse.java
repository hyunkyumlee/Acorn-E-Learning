package com.acorn.elearning.common.response;

import java.util.List;

public record ApiResponse<T>(boolean success, String message, T data, ApiError error) {
    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(true, "OK", data, null); }
    public static <T> ApiResponse<T> success(String message, T data) { return new ApiResponse<>(true, message, data, null); }
    public static <T> ApiResponse<T> fail(String message, String code, String detail) { return new ApiResponse<>(false, message, null, new ApiError(code, detail, List.of())); }
    public static <T> ApiResponse<T> fail(String message, String code, String detail, List<FieldError> fieldErrors) { return new ApiResponse<>(false, message, null, new ApiError(code, detail, fieldErrors)); }
    public record ApiError(String code, String detail, List<FieldError> fieldErrors) {}
    public record FieldError(String field, String message) {}
}
