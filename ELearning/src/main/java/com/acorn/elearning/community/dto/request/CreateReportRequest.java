package com.acorn.elearning.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreateReportRequest(@NotBlank String requestId, Map<String, Object> payload) {}
