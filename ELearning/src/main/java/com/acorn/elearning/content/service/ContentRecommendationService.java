package com.acorn.elearning.content.service;

import com.acorn.elearning.content.dto.response.ContentRecommendationListResponse;
import com.acorn.elearning.content.mapper.ContentRecommendationMapper;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentRecommendationService {
    private final ContentRecommendationMapper contentRecommendationMapper;

    public ContentRecommendationService(ContentRecommendationMapper contentRecommendationMapper) {
        this.contentRecommendationMapper = contentRecommendationMapper;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional(readOnly = true)
    public ContentRecommendationListResponse recommendations(Long subjectId, String contentType, String slot) {
        String normalizedContentType = normalize(contentType);
        String normalizedSlot = normalize(slot);
        return ContentRecommendationListResponse.of(
                contentRecommendationMapper.findActive(subjectId, normalizedContentType, normalizedSlot),
                subjectId,
                normalizedContentType,
                normalizedSlot
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
