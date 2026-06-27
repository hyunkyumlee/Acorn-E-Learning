package com.acorn.elearning.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH-401", "로그인이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-FORBIDDEN", "권한이 없습니다."),
    COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION-400", "입력값이 올바르지 않습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-NOT-FOUND", "요청한 대상을 찾을 수 없습니다."),
    COMMON_IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "COMMON-IDEMPOTENCY-KEY-REQUIRED", "중복 방지 토큰이 필요합니다."),
    COMMON_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "COMMON-IDEMPOTENCY-CONFLICT", "중복 요청 정보가 일치하지 않습니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
