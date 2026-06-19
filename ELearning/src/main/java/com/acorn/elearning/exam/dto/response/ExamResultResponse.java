package com.acorn.elearning.exam.dto.response;

import java.util.Map;

public record ExamResultResponse(String status, Map<String, Object> data) {
    public static ExamResultResponse stub() { return new ExamResultResponse("SKELETON", Map.of()); }
}
