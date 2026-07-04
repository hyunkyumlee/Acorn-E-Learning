package com.acorn.elearning.analysis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisLearningProgressStat {
    private Long nodeId;
    private String nodeTitle;
    private String nodeType;
    private Integer planetNo;
    private Boolean lessonCompleted;
    private Boolean practicePassed;
    private BigDecimal progressRate;
    private LocalDateTime completedAt;
}
