package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record CommunityProfileResponse(String status, Map<String, Object> data) {
    public static CommunityProfileResponse stub() { return new CommunityProfileResponse("SKELETON", Map.of()); }
}
