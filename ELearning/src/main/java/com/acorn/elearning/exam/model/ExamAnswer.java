package com.acorn.elearning.exam.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamAnswer {
    private Long answerId;
    private Long examId;
    private Long aiProblemId;
    private String answerText;
    private Integer passedCaseCount;
    private Boolean isCorrect;
    private String aiReview;
    private String testCaseResult;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
