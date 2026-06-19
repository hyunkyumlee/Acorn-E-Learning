package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record PostPageResponse(String status, Map<String, Object> data) {
    public static PostPageResponse stub() { return new PostPageResponse("SKELETON", Map.of()); }
}
