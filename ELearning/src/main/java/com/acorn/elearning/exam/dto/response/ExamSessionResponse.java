package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ExamSessionResponse(
        Long examId,
        Long subjectId,
        String levelCode,
        String status,
        String resultStatus,
        Integer totalProblemCount,
        Integer correctCount,
        List<Problem> problems
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ExamSessionResponse from(ExamSession session, List<AiExamProblem> problems) {
        return from(session, problems, List.of());
    }

    public static ExamSessionResponse from(ExamSession session, List<AiExamProblem> problems, List<ExamAnswer> answers) {
        Map<Long, ExamAnswer> answersByProblemId = answers.stream()
                .collect(Collectors.toMap(ExamAnswer::getAiProblemId, Function.identity()));
        return new ExamSessionResponse(
                session.getExamId(),
                session.getSubjectId(),
                session.getLevelCode(),
                session.getStatus(),
                session.getResultStatus(),
                session.getTotalProblemCount(),
                session.getCorrectCount(),
                problems.stream()
                        .map(problem -> Problem.from(problem, answersByProblemId.get(problem.getAiProblemId())))
                        .toList());
    }

    public record Problem(Long aiProblemId, Integer problemNo, String prompt, String status, String answerText, boolean answered) {
        private static final String DEFAULT_STARTER_CODE = """
                import java.util.Scanner;

                public class Solution {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);

                        // TODO 여기에 문제 풀이 로직을 작성하세요.
                        // 예: int n = scanner.nextInt();

                        // TODO 정답을 System.out.println으로 출력하세요.
                    }
                }
                """;

        static Problem from(AiExamProblem problem) {
            return from(problem, null);
        }

        static Problem from(AiExamProblem problem, ExamAnswer answer) {
            String answerText = answer == null || answer.getAnswerText() == null ? "" : answer.getAnswerText();
            boolean answered = !answerText.isBlank();
            return new Problem(
                    problem.getAiProblemId(),
                    problem.getProblemNo(),
                    problem.getPrompt(),
                    problem.getStatus(),
                    answered ? answerText : starterCode(problem),
                    answered);
        }

        private static String starterCode(AiExamProblem problem) {
            String generatedStarterCode = generatedStarterCode(problem);
            if (!generatedStarterCode.isBlank()) {
                return generatedStarterCode;
            }
            return DEFAULT_STARTER_CODE;
        }

        private static String generatedStarterCode(AiExamProblem problem) {
            if (problem.getAiRawResponse() == null || problem.getProblemNo() == null) {
                return "";
            }
            try {
                JsonNode problems = OBJECT_MAPPER.readTree(problem.getAiRawResponse()).path("problems");
                if (!problems.isArray() || problems.size() < problem.getProblemNo()) {
                    return "";
                }
                JsonNode starterCode = problems.get(problem.getProblemNo() - 1).path("starterCode");
                return starterCode.isTextual() ? starterCode.asText() : "";
            } catch (JacksonException exception) {
                return "";
            }
        }
    }
}
