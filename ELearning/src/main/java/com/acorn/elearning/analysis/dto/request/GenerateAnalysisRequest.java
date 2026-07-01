package com.acorn.elearning.analysis.dto.request;

import jakarta.validation.constraints.NotNull;

public record GenerateAnalysisRequest(@NotNull Long examId) {}
