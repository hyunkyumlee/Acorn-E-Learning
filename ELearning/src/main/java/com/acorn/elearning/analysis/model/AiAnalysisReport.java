package com.acorn.elearning.analysis.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiAnalysisReport {
    private Long reportId;
    private Long userId;
    private Long examId;
    private String status;
    private String freeSummary;
    private String premiumDetail;
    private String analysisErrorCode;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
