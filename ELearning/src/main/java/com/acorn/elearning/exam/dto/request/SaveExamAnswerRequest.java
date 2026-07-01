package com.acorn.elearning.exam.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SaveExamAnswerRequest(@NotBlank String answerText) {}
