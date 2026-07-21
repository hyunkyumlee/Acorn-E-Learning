package com.acorn.elearning.exam.support;

import com.acorn.elearning.exam.model.AiExamProblem;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class ExamStarterCodeResolver {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

    private ExamStarterCodeResolver() {}

    public static String starterCode(AiExamProblem ignoredProblem) {
        return DEFAULT_STARTER_CODE;
    }

    public static String defaultStarterCode() {
        return DEFAULT_STARTER_CODE;
    }

    public static Optional<String> solutionCode(AiExamProblem problem) {
        if (problem == null || problem.getProblemNo() == null || problem.getProblemNo() <= 0
                || problem.getAiRawResponse() == null || problem.getAiRawResponse().isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode problems = OBJECT_MAPPER.readTree(problem.getAiRawResponse()).path("problems");
            int problemIndex = problem.getProblemNo() - 1;
            if (!problems.isArray() || problemIndex >= problems.size()) {
                return Optional.empty();
            }

            JsonNode solutionCode = problems.get(problemIndex).path("starterCode");
            if (!solutionCode.isTextual() || solutionCode.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(solutionCode.asText());
        } catch (JacksonException exception) {
            return Optional.empty();
        }
    }
}
