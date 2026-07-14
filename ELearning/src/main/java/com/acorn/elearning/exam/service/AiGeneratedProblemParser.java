package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
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
    private static final List<String> RESTRICTED_STARTER_TOKENS = List.of(
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

    AiGeneratedProblemParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    List<GeneratedProblem> parse(String content, ExamLearningScope learningScope, int problemCount) {
        try {
            JsonNode problems = objectMapper.readTree(content).path("problems");
            if (!problems.isArray() || problems.size() < problemCount) {
                throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI가 생성한 문제 수가 부족합니다.");
            }
            return parseProblems(problems, content, learningScope, problemCount);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI가 생성한 문제 형식이 올바르지 않습니다.");
        }
    }

    private List<GeneratedProblem> parseProblems(JsonNode problems, String content, ExamLearningScope learningScope, int problemCount)
            throws JacksonException {
        String learningScopeText = learningScopeText(learningScope);
        List<GeneratedProblem> parsed = new ArrayList<>();
        for (int index = 0; index < problemCount; index++) {
            JsonNode problem = problems.get(index);
            String starterCode = requiredText(problem, "starterCode");
            validateStarterCode(starterCode, learningScopeText);
            parsed.add(new GeneratedProblem(
                    ExamPromptNormalizer.normalize(requiredText(problem, "prompt")),
                    objectMapper.writeValueAsString(validTestCases(problem)),
                    content));
        }
        return parsed;
    }

    private JsonNode validTestCases(JsonNode problem) {
        JsonNode testCases = problem.path("testCases");
        if (!testCases.isArray() || testCases.size() == 0) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI가 생성한 테스트케이스가 비어 있습니다.");
        }
        for (JsonNode testCase : testCases) {
            requireTestCaseText(testCase, "input");
            requireTestCaseText(testCase, "expectedOutput");
        }
        return testCases;
    }

    private void validateStarterCode(String starterCode, String learningScopeText) {
        String normalizedStarterCode = starterCode.toLowerCase(Locale.ROOT);
        for (String token : RESTRICTED_STARTER_TOKENS) {
            if (normalizedStarterCode.contains(token.toLowerCase(Locale.ROOT)) && !allowedByLearningScope(token, learningScopeText)) {
                throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "학습 범위에 없는 starterCode API가 포함되어 있습니다.");
            }
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
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI가 생성한 문제 본문이 비어 있습니다.");
        }
        return field.asText();
    }

    private void requireTestCaseText(JsonNode node, String fieldName) {
        if (!node.path(fieldName).isTextual()) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI가 생성한 테스트케이스 형식이 올바르지 않습니다.");
        }
    }

    record GeneratedProblem(String prompt, String testCaseSpec, String rawResponse) {}
}
