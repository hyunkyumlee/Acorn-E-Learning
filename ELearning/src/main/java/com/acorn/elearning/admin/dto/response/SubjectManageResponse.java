package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record SubjectManageResponse(String status, Map<String, Object> data) {
    public static SubjectManageResponse stub() { return new SubjectManageResponse("SKELETON", Map.of()); }
}
