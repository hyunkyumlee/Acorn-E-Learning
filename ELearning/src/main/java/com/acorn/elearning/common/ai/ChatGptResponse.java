package com.acorn.elearning.common.ai;

import java.util.Map;

public record ChatGptResponse(String status, String provider, String baseUrl, String model, String purpose, Map<String, Object> metadata) {
    public static ChatGptResponse skeleton(String provider, String baseUrl, String model, String purpose, boolean apiKeyConfigured) {
        return new ChatGptResponse(
                "SKELETON",
                provider,
                baseUrl,
                model,
                purpose,
                Map.of("apiKeyConfigured", apiKeyConfigured));
    }
}
