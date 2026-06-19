package com.acorn.elearning.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ExamSubmitRequest(@NotBlank String requestId, Map<String, Object> payload) {}
