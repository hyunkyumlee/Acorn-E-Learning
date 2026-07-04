package com.acorn.elearning.analysis.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisExamSummary {
    private Long examId;
    private Long userId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String levelCode;
    private String status;
    private String resultStatus;
    private Integer totalProblemCount;
    private Integer correctCount;
    private Integer retryCount;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
}
