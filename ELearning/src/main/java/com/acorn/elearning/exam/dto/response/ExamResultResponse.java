package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record ExamResultResponse(
        Long examId,
        String status,
        String resultStatus,
        Integer correctCount,
        Integer totalProblemCount,
        List<Answer> answers
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            String aiReview,
            String explanation,
            String codeReview
    ) {
        static Answer from(ExamAnswer answer) {
            ReviewText reviewText = ReviewText.from(answer.getAiReview());
            return new Answer(
                    answer.getAnswerId(),
                    answer.getAiProblemId(),
                    answer.getPassedCaseCount(),
                    answer.getIsCorrect(),
                    answer.getAiReview(),
                    reviewText.explanation(),
                    reviewText.codeReview());
        }
    }

    private record ReviewText(String explanation, String codeReview) {
        private static ReviewText from(String rawReview) {
            if (rawReview == null || rawReview.isBlank()) {
                return new ReviewText("", "");
            }
            try {
                JsonNode node = OBJECT_MAPPER.readTree(rawReview);
                String explanation = node.path("explanation").asText("");
                String codeReview = node.path("codeReview").asText("");
                if (!explanation.isBlank() || !codeReview.isBlank()) {
                    return new ReviewText(explanation, codeReview);
                }
                return new ReviewText(rawReview, "");
            } catch (JacksonException exception) {
                return new ReviewText(rawReview, "");
            }
        }
    }
}
