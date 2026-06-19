package com.acorn.elearning.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreatePostRequest(@NotBlank String requestId, Map<String, Object> payload) {}
