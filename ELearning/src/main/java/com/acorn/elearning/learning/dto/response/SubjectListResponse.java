package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record SubjectListResponse(String status, Map<String, Object> data) {
    public static SubjectListResponse stub() { return new SubjectListResponse("SKELETON", Map.of()); }
}
