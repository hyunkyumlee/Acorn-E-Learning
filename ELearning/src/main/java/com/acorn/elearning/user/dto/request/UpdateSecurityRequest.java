package com.acorn.elearning.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record UpdateSecurityRequest(@NotBlank String requestId, Map<String, Object> payload) {}
