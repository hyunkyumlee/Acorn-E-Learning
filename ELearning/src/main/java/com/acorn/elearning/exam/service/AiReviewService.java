package com.acorn.elearning.exam.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiReviewService {
    public Map<String, Object> generateExplanation(String action) {
        return Map.of("action", action, "purpose", "explanation", "provider", "openai", "status", "SKELETON");
    }

    public Map<String, Object> reviewCode(String action) {
        return Map.of("action", action, "purpose", "code-review", "provider", "openai", "status", "SKELETON");
    }
}
