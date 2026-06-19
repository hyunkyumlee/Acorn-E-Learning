package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record CurriculumResponse(String status, Map<String, Object> data) {
    public static CurriculumResponse stub() { return new CurriculumResponse("SKELETON", Map.of()); }
}
