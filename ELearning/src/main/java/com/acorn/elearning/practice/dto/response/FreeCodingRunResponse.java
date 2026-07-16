package com.acorn.elearning.practice.dto.response;

import com.acorn.elearning.exam.service.TestCaseExecutionService.CodeExecutionResult;

public record FreeCodingRunResponse(
        String status,
        boolean success,
        long elapsedMs,
        String message,
        String output
) {
    public static FreeCodingRunResponse from(CodeExecutionResult result, long elapsedMs) {
        return new FreeCodingRunResponse(
                result.status(),
                result.success(),
                elapsedMs,
                message(result.status()),
                result.output());
    }

    private static String message(String status) {
        return switch (status) {
            case "SUCCESS" -> "실행 완료";
            case "COMPILE_ERROR" -> "컴파일 오류";
            case "RUNTIME_ERROR" -> "실행 오류";
            case "TIMEOUT" -> "실행 시간 초과";
            case "SECURITY_VIOLATION" -> "보안 정책으로 실행 차단";
            default -> "실행 실패";
        };
    }
}
