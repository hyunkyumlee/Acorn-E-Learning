package com.acorn.elearning.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH-401", "로그인이 필요해"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-FORBIDDEN", "권한이 없어"),
    COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION-400", "입력값이 올바르지 않아"),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-NOT-FOUND", "대상을 찾을 수 없어"),
    COMMON_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "COMMON-IDEMPOTENCY-CONFLICT", "중복 요청이 충돌했어"),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했어");
    private final HttpStatus status;
    private final String code;
    private final String message;
    ErrorCode(HttpStatus status, String code, String message) { this.status = status; this.code = code; this.message = message; }
    public HttpStatus status() { return status; }
    public String code() { return code; }
    public String message() { return message; }
}
