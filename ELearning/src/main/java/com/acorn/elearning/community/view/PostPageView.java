package com.acorn.elearning.community.view;

import java.util.Map;

public record PostPageView(String title, String status, Map<String, Object> attributes) {
    public static PostPageView stub(String title) { return new PostPageView(title, "SKELETON", Map.of()); }
}
