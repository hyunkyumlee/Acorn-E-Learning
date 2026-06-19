package com.acorn.elearning.user.view;

import java.util.Map;

public record SocialAccountView(String title, String status, Map<String, Object> attributes) {
    public static SocialAccountView stub(String title) { return new SocialAccountView(title, "SKELETON", Map.of()); }
}
