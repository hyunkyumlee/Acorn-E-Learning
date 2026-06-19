package com.acorn.elearning.exam.dto.response;

import java.util.Map;

public record ExamSessionResponse(String status, Map<String, Object> data) {
    public static ExamSessionResponse stub() { return new ExamSessionResponse("SKELETON", Map.of()); }
}
