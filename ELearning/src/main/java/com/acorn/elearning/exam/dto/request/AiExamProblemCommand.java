package com.acorn.elearning.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record AiExamProblemCommand(@NotBlank String promptVersion, Map<String, Object> payload) {}
