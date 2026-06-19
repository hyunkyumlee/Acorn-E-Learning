package com.acorn.elearning.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreateExamRequest(@NotBlank String requestId, Map<String, Object> payload) {}
