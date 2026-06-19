package com.acorn.elearning.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreatePracticeSetRequest(@NotBlank String requestId, Map<String, Object> payload) {}
