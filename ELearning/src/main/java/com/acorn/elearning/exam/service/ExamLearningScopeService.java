package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class ExamLearningScopeService {
    private static final int MAX_TEXT_LENGTH = 320;

    private final ExamLearningScopeMapper examLearningScopeMapper;

    public ExamLearningScopeService(ExamLearningScopeMapper examLearningScopeMapper) {
        this.examLearningScopeMapper = examLearningScopeMapper;
    }

    public ExamLearningScope build(Long userId, Long subjectId, String levelCode) {
        List<LearnedItem> learnedItems = examLearningScopeMapper
                .findCompletedLessonScope(userId, subjectId, levelCode)
                .stream()
                .map(this::toLearnedItem)
                .toList();

        if (learnedItems.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "완료한 필수 레슨 범위가 없어 AI 시험 문제를 생성할 수 없습니다.");
        }

        return new ExamLearningScope(
                learnedItems,
                allowedConcepts(learnedItems),
                """
                        starterCode는 java.util.Scanner 기반으로 작성합니다.
                        입력을 읽는 코드는 starterCode 안에 미리 제공합니다.
                        사용자는 TODO 주석 아래의 풀이 로직과 출력만 작성하면 되도록 합니다.
                        learnedItems에 직접 등장하지 않는 BufferedReader, InputStreamReader, StringTokenizer, Collection, recursion, sorting API는 사용하거나 요구하지 않습니다.
                        """);
    }

    public ExamLearningEligibility eligibility(Long userId, Long subjectId, String levelCode) {
        int requiredLessonCount = examLearningScopeMapper.countRequiredLessons(subjectId, levelCode);
        int incompleteRequiredLessonCount =
                examLearningScopeMapper.countIncompleteRequiredLessons(userId, subjectId, levelCode);

        boolean eligible = requiredLessonCount > 0 && incompleteRequiredLessonCount == 0;
        String message;
        if (requiredLessonCount == 0) {
            message = "응시 가능한 필수 레슨이 없습니다.";
        } else if (eligible) {
            message = "AI 코딩테스트를 시작할 수 있습니다.";
        } else {
            message = "필수 레슨의 이론 학습과 문제풀이를 모두 완료해야 AI 코딩테스트를 시작할 수 있습니다.";
        }

        return new ExamLearningEligibility(
                eligible,
                incompleteRequiredLessonCount,
                message
        );
    }

    private LearnedItem toLearnedItem(ExamLearningScopeItem item) {
        return new LearnedItem(
                item.getSourceType(),
                normalize(item.getNodeTitle()),
                normalize(item.getTitle()),
                normalize(item.getSummary()),
                normalize(item.getExampleCode()));
    }

    private List<String> allowedConcepts(List<LearnedItem> learnedItems) {
        Set<String> concepts = new TreeSet<>();
        concepts.add("public class Solution");
        concepts.add("main method");
        concepts.add("java.util.Scanner starter input");
        concepts.add("System.out.println");

        String scopeText = learnedItems.stream()
                .flatMap(item -> Stream.of(item.nodeTitle(), item.title(), item.summary(), item.exampleCode()))
                .map(value -> value.toLowerCase(Locale.ROOT))
                .reduce("", (left, right) -> left + " " + right);

        addWhenPresent(concepts, scopeText, "변수", "variables");
        addWhenPresent(concepts, scopeText, "자료형", "primitive types");
        addWhenPresent(concepts, scopeText, "if", "if/else");
        addWhenPresent(concepts, scopeText, "switch", "switch");
        addWhenPresent(concepts, scopeText, "for", "for loop");
        addWhenPresent(concepts, scopeText, "while", "while loop");
        addWhenPresent(concepts, scopeText, "배열", "array");
        addWhenPresent(concepts, scopeText, "int[]", "array");
        addWhenPresent(concepts, scopeText, "메서드", "method");
        addWhenPresent(concepts, scopeText, "return", "return");
        return List.copyOf(concepts);
    }

    private void addWhenPresent(Set<String> concepts, String scopeText, String marker, String concept) {
        if (scopeText.contains(marker.toLowerCase(Locale.ROOT))) {
            concepts.add(concept);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_TEXT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_TEXT_LENGTH);
    }

    public record ExamLearningScope(
            List<LearnedItem> learnedItems,
            List<String> allowedConcepts,
            String starterCodePolicy
    ) {}

    public record ExamLearningEligibility(
            boolean eligible,
            int incompleteRequiredLessonCount,
            String message
    ) {}

    public record LearnedItem(
            String sourceType,
            String nodeTitle,
            String title,
            String summary,
            String exampleCode
    ) {}
}
