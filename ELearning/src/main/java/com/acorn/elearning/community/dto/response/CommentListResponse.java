package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record CommentListResponse(String status, Map<String, Object> data) {
    public static CommentListResponse stub() { return new CommentListResponse("SKELETON", Map.of()); }
}
