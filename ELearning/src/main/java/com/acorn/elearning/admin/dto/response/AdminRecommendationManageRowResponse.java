package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;

public record AdminRecommendationManageRowResponse(
        Long contentId,
        Long subjectId,
        String subjectName,
        String title,
        String url,
        String contentType,
        String recommendationSlot,
        Boolean isActive,
        LocalDateTime updatedAt
) {}
