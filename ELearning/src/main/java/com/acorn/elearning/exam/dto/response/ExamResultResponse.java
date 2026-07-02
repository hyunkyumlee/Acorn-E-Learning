package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;

public record ExamResultResponse(
        Long examId,
        String status,
        String resultStatus,
        Integer correctCount,
        Integer totalProblemCount,
        List<Answer> answers
) {
    public static ExamResultResponse from(ExamSession session, List<ExamAnswer> answers) {
        return new ExamResultResponse(
                session.getExamId(),
                session.getStatus(),
                session.getResultStatus(),
                session.getCorrectCount(),
                session.getTotalProblemCount(),
                answers.stream().map(Answer::from).toList());
    }

    public record Answer(
            Long answerId,
            Long aiProblemId,
            Integer passedCaseCount,
            Boolean correct,
            String aiReview
    ) {
        static Answer from(ExamAnswer answer) {
            return new Answer(
                    answer.getAnswerId(),
                    answer.getAiProblemId(),
                    answer.getPassedCaseCount(),
                    answer.getIsCorrect(),
                    answer.getAiReview());
        }
    }
}
