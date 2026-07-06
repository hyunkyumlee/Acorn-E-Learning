package com.acorn.elearning.exam.support;

import com.acorn.elearning.exam.model.AiExamProblem;
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

    public static String starterCode(AiExamProblem problem) {
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
