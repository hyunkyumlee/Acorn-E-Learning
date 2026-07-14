package com.acorn.elearning.admin.dto.response;

public record AdminRecommendationDetailResponse(
        Long contentId,
        Long subjectId,
        String subjectName,
        String title,
        String url,
        String contentType,
        String recommendationSlot,
        Boolean isActive) {
}
