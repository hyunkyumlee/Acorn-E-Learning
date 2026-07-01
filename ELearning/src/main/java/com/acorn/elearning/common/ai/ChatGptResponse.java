package com.acorn.elearning.common.ai;

import java.util.Map;

public record ChatGptResponse(
        String status,
        String provider,
        String baseUrl,
        String model,
        String purpose,
        String content,
        String rawResponse,
        Map<String, Object> metadata
) {}
