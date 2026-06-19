package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record AttachmentListResponse(String status, Map<String, Object> data) {
    public static AttachmentListResponse stub() { return new AttachmentListResponse("SKELETON", Map.of()); }
}
