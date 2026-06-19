package com.acorn.elearning.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record WrongAnswerRetryRequest(@NotBlank String requestId, Map<String, Object> payload) {}
