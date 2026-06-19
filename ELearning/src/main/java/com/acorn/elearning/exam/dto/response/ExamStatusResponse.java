package com.acorn.elearning.exam.dto.response;

import java.util.Map;

public record ExamStatusResponse(String status, Map<String, Object> data) {
    public static ExamStatusResponse stub() { return new ExamStatusResponse("SKELETON", Map.of()); }
}
