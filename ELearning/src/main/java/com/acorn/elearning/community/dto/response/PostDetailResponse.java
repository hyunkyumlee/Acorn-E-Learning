package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record PostDetailResponse(String status, Map<String, Object> data) {
    public static PostDetailResponse stub() { return new PostDetailResponse("SKELETON", Map.of()); }
}
