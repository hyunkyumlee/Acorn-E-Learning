package com.acorn.elearning.community.view;

import java.util.Map;

public record CommunityProfileView(String title, String status, Map<String, Object> attributes) {
    public static CommunityProfileView stub(String title) { return new CommunityProfileView(title, "IMPLEMENTED", Map.of()); }
}
