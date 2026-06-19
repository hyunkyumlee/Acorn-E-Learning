package com.acorn.elearning.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record SignupRequest(@NotBlank String requestId, Map<String, Object> payload) {}
