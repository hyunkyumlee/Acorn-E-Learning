package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record CommentResponse(String status, Map<String, Object> data) {
    public static CommentResponse stub() { return new CommentResponse("SKELETON", Map.of()); }
}
