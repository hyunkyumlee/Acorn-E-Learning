package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record NoticePageResponse(String status, Map<String, Object> data) {
    public static NoticePageResponse stub() { return new NoticePageResponse("SKELETON", Map.of()); }
}
