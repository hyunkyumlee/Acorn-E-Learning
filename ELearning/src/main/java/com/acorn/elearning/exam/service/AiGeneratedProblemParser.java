package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.service.ExamLearningScopeService.ExamLearningScope;
import com.acorn.elearning.exam.service.ExamLearningScopeService.LearnedItem;
import com.acorn.elearning.exam.support.ExamPromptNormalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class AiGeneratedProblemParser {
    private static final List<String> RESTRICTED_API_TOKENS = List.of(
            "BufferedReader",
            "InputStreamReader",
            "StringTokenizer",
            "ArrayList",
            "HashMap",
            "Collections",
            "Arrays.",
            "java.io",
            "List<",
            "Map<",
            "Set<",
            "Queue<",
            "Deque<",
            "PriorityQueue");

    private final ObjectMapper objectMapper;
    private final TestCaseExecutionService testCaseExecutionService;

    AiGeneratedProblemParser(ObjectMapper objectMapper, TestCaseExecutionService testCaseExecutionService) {
        this.objectMapper = objectMapper;
        this.testCaseExecutionService = testCaseExecutionService;
    }

    List<GeneratedProblem> parse(String content, ExamLearningScope learningScope, int problemCount) {
        try {
            String normalizedContent = normalizeJsonContent(content);
            JsonNode problems = objectMapper.readTree(normalizedContent).path("problems");
            if (!problems.isArray() || problems.size() < problemCount) {
                throw invalidGeneratedProblem("AI가 생성한 문제 수가 부족합니다.");
            }
            return parseProblems(problems, normalizedContent, learningScope, problemCount);
        } catch (JacksonException exception) {
            throw invalidGeneratedProblem("AI가 생성한 문제 형식이 올바르지 않습니다.");
        }
    }

    private List<GeneratedProblem> parseProblems(JsonNode problems, String content, ExamLearningScope learningScope, int problemCount)
            throws JacksonException {
        String learningScopeText = learningScopeText(learningScope);
        List<GeneratedProblem> parsed = new ArrayList<>();
        for (int index = 0; index < problemCount; index++) {
            JsonNode problem = problems.get(index);
            String solutionCode = requiredText(problem, "solutionCode");
            validateSolutionCode(solutionCode, learningScopeText);
            String testCaseSpec = objectMapper.writeValueAsString(validTestCases(problem));
            validateSolutionExecution(solutionCode, testCaseSpec);
            parsed.add(new GeneratedProblem(
                    ExamPromptNormalizer.normalize(requiredText(problem, "prompt")),
                    testCaseSpec,
                    content));
        }
        return parsed;
    }

    private JsonNode validTestCases(JsonNode problem) {
        JsonNode testCases = problem.path("testCases");
        if (!testCases.isArray() || testCases.size() == 0) {
            throw invalidGeneratedProblem("AI가 생성한 테스트케이스가 비어 있습니다.");
        }
        for (JsonNode testCase : testCases) {
            requireTestCaseText(testCase, "input");
            requireTestCaseText(testCase, "expectedOutput");
        }
        return testCases;
    }

    private void validateSolutionCode(String solutionCode, String learningScopeText) {
        if (solutionCode.contains("TODO")) {
            throw invalidGeneratedProblem("AI가 생성한 solutionCode에 미완성 로직이 있습니다.");
        }
        if (!solutionCode.contains("public class Solution")) {
            throw invalidGeneratedProblem("AI가 생성한 solutionCode 형식이 올바르지 않습니다.");
        }
        validateAllowedApis(solutionCode, learningScopeText, "solutionCode");
    }

    private void validateAllowedApis(String code, String learningScopeText, String codeFieldName) {
        String normalizedCode = code.toLowerCase(Locale.ROOT);
        for (String token : RESTRICTED_API_TOKENS) {
            if (normalizedCode.contains(token.toLowerCase(Locale.ROOT)) && !allowedByLearningScope(token, learningScopeText)) {
                throw invalidGeneratedProblem(
                        "학습 범위에 없는 " + codeFieldName + " API가 포함되어 있습니다.");
            }
        }
    }

    private void validateSolutionExecution(String solutionCode, String testCaseSpec) {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(testCaseSpec);
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText(solutionCode);
        TestCaseExecutionResult result = testCaseExecutionService.execute(problem, answer);
        if (!result.passed()) {
            throw invalidGeneratedProblem(
                    "AI가 생성한 solutionCode가 테스트케이스를 통과하지 못했습니다.");
        }
    }

    private boolean allowedByLearningScope(String token, String learningScopeText) {
        String normalizedToken = token.toLowerCase(Locale.ROOT);
        if ("java.io".equals(normalizedToken)) {
            return learningScopeText.contains("java.io")
                    || learningScopeText.contains("bufferedreader")
                    || learningScopeText.contains("inputstreamreader");
        }
        return learningScopeText.contains(normalizedToken);
    }

    private String learningScopeText(ExamLearningScope learningScope) {
        Stream<String> itemTexts = learningScope.learnedItems().stream()
                .flatMap(this::learnedItemTexts);
        return Stream.concat(itemTexts, learningScope.allowedConcepts().stream())
                .map(value -> value == null ? "" : value.toLowerCase(Locale.ROOT))
                .reduce("", (left, right) -> left + " " + right);
    }

    private Stream<String> learnedItemTexts(LearnedItem item) {
        return Stream.of(item.sourceType(), item.nodeTitle(), item.title(), item.summary(), item.exampleCode());
    }

    private String requiredText(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (!field.isTextual() || field.asText().isBlank()) {
            throw invalidGeneratedProblem("AI가 생성한 문제 본문이 비어 있습니다.");
        }
        return field.asText();
    }

    private void requireTestCaseText(JsonNode node, String fieldName) {
        if (!node.path(fieldName).isTextual()) {
            throw invalidGeneratedProblem("AI가 생성한 테스트케이스 형식이 올바르지 않습니다.");
        }
    }

    private String normalizeJsonContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (!normalized.startsWith("```")) {
            return normalized;
        }

        int firstLineEnd = normalized.indexOf('\n');
        if (firstLineEnd < 0 || !normalized.endsWith("```")) {
            return normalized;
        }

        String fenceLanguage = normalized.substring(3, firstLineEnd).trim();
        if (!fenceLanguage.isEmpty() && !"json".equalsIgnoreCase(fenceLanguage)) {
            return normalized;
        }
        return normalized.substring(firstLineEnd + 1, normalized.length() - 3).trim();
    }

    private InvalidGeneratedProblemException invalidGeneratedProblem(String message) {
        return new InvalidGeneratedProblemException(message);
    }

    record GeneratedProblem(String prompt, String testCaseSpec, String rawResponse) {}
}

final class InvalidGeneratedProblemException extends BusinessException {
    InvalidGeneratedProblemException(String message) {
        super(ErrorCode.COMMON_INTERNAL_ERROR, message);
    }
}
