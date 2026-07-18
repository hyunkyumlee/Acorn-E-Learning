package com.acorn.elearning.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH-401", "로그인이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-FORBIDDEN", "권한이 없습니다."),

    //이정하 작업 - auth 전용 실패 코드
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-INVALID-CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH-SUSPENDED", "정지된 계정입니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-USER-NOT-FOUND", "사용자를 찾을 수 없습니다."),
    AUTH_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "AUTH-EMAIL-DUPLICATED", "이미 사용 중인 이메일입니다."),
    AUTH_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "AUTH-NICKNAME-DUPLICATED", "이미 사용 중인 닉네임입니다."),
    AUTH_SOCIAL_PENDING_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-SOCIAL-PENDING-EXPIRED", "소셜 가입 정보가 만료되었습니다. 다시 시도해 주세요."), // [추가] 소셜 회원가입 대기정보 만료

    //이정하 작업 - 비밀번호 찾기(재설정) 실패 코드
    AUTH_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH-RESET-TOKEN-INVALID", "유효하지 않은 재설정 링크입니다. 비밀번호 찾기를 다시 요청해 주세요."),
    AUTH_RESET_TOKEN_EXPIRED(HttpStatus.GONE, "AUTH-RESET-TOKEN-EXPIRED", "재설정 링크가 만료되었습니다. 비밀번호 찾기를 다시 요청해 주세요."),
    AUTH_RESET_TOKEN_USED(HttpStatus.CONFLICT, "AUTH-RESET-TOKEN-USED", "이미 사용된 재설정 링크입니다. 비밀번호 찾기를 다시 요청해 주세요."),
    AUTH_MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-MAIL-SEND-FAILED", "메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    AUTH_PASSWORD_TOO_GUESSABLE(HttpStatus.BAD_REQUEST, "AUTH-PASSWORD-TOO-GUESSABLE", "비밀번호에 닉네임이나 이메일 아이디를 포함할 수 없습니다."),


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