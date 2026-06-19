package com.acorn.elearning.content.dto.response;

import java.util.Map;

public record ContentRecommendationListResponse(String status, Map<String, Object> data) {
    public static ContentRecommendationListResponse stub() { return new ContentRecommendationListResponse("SKELETON", Map.of()); }
}
