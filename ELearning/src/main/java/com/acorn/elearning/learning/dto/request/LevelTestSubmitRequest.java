package com.acorn.elearning.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record LevelTestSubmitRequest(@NotBlank String requestId, Map<String, Object> payload) {}
