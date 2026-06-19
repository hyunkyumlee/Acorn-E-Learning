package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record ProblemManageResponse(String status, Map<String, Object> data) {
    public static ProblemManageResponse stub() { return new ProblemManageResponse("SKELETON", Map.of()); }
}
