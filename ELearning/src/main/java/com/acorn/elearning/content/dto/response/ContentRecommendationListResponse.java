package com.acorn.elearning.content.dto.response;

import com.acorn.elearning.content.model.ContentRecommendation;
import java.util.List;

public record ContentRecommendationListResponse(
        List<ContentRecommendation> recommendations,
        Long subjectId,
        String contentType,
        String slot,
        int total
) {
    public static ContentRecommendationListResponse of(
            List<ContentRecommendation> recommendations,
            Long subjectId,
            String contentType,
            String slot
    ) {
        return new ContentRecommendationListResponse(
                recommendations,
                subjectId,
                contentType,
                slot,
                recommendations.size()
        );
    }
}
