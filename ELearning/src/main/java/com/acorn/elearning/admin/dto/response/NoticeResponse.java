package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record NoticeResponse(String status, Map<String, Object> data) {
    public static NoticeResponse stub() { return new NoticeResponse("SKELETON", Map.of()); }
}
