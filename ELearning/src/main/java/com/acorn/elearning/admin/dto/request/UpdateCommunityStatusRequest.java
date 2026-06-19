package com.acorn.elearning.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record UpdateCommunityStatusRequest(@NotBlank String requestId, Map<String, Object> payload) {}
