package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record CurriculumNodeManageResponse(String status, Map<String, Object> data) {
    public static CurriculumNodeManageResponse stub() { return new CurriculumNodeManageResponse("SKELETON", Map.of()); }
}
