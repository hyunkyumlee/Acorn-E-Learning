package com.acorn.elearning.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExamRequest(
        @NotNull Long subjectId,
        @NotBlank String levelCode
) {}
