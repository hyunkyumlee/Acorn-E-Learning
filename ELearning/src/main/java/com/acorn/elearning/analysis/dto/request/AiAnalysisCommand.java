package com.acorn.elearning.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record AiAnalysisCommand(@NotBlank String purpose, Map<String, Object> payload) {}
