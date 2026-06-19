package com.acorn.elearning.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record PracticeAnswerRequest(@NotBlank String requestId, Map<String, Object> payload) {}
