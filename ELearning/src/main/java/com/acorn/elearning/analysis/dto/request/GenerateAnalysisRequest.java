package com.acorn.elearning.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record GenerateAnalysisRequest(@NotBlank String requestId, Map<String, Object> payload) {}
