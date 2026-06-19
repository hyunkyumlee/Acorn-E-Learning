package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record LearningDashboardResponse(String status, Map<String, Object> data) {
    public static LearningDashboardResponse stub() { return new LearningDashboardResponse("SKELETON", Map.of()); }
}
