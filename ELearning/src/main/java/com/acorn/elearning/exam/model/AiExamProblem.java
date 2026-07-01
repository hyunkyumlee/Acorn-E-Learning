package com.acorn.elearning.exam.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiExamProblem {
    private Long aiProblemId;
    private Long examId;
    private Integer problemNo;
    private String prompt;
    private String testCaseSpec;
    private String aiRawResponse;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
