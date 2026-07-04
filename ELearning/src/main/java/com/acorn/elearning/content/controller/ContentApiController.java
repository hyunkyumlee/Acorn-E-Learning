package com.acorn.elearning.content.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.content.dto.response.ContentRecommendationListResponse;
import com.acorn.elearning.content.service.ContentRecommendationService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContentApiController {
    private final ContentRecommendationService contentRecommendationService;

    public ContentApiController(ContentRecommendationService contentRecommendationService) {
        this.contentRecommendationService = contentRecommendationService;
    }

    @GetMapping("/api/content/recommendations")
    public ApiResponse<ContentRecommendationListResponse> recommendations(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false, name = "slot") String slot
    ) {
        return ApiResponse.success(contentRecommendationService.recommendations(subjectId, contentType, slot));
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "IMPLEMENTED"));
    }
}
