package com.acorn.elearning.analysis.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisPracticeSummary {
    private Integer totalAttempts;
    private Integer passedAttempts;
    private Integer totalProblems;
    private Integer correctProblems;
    private BigDecimal averageCorrectRate;
}
