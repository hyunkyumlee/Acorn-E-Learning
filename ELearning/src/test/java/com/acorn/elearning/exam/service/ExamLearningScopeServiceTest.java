package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ExamLearningScopeServiceTest {

    @Test
    void build_uses_all_completed_required_lessons_without_individual_practice_items() {
        List<ExamLearningScopeItem> lessons = IntStream.rangeClosed(1, 20)
                .mapToObj(index -> item(
                        "LESSON",
                        "Java 반복문 행성",
                        "레슨 " + index,
                        "for 문으로 반복 실행합니다.",
                        "for (int i = 0; i < " + index + "; i++) {}",
                        index))
                .toList();
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(lessons));

        ExamLearningScopeService.ExamLearningScope scope = service.build(2L, 1L, "BRONZE");

        assertEquals(20, scope.learnedItems().size());
        assertEquals("레슨 20", scope.learnedItems().get(19).title());
        assertTrue(scope.allowedConcepts().contains("for loop"));
        assertTrue(scope.starterCodePolicy().contains("BufferedReader"));
    }

    @Test
    void build_throws_validation_error_when_scope_is_empty() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(List.of()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.build(2L, 1L, "BRONZE"));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
    }

    @Test
    void eligibility_allows_exam_when_required_lessons_are_complete() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(
                List.of(item("LESSON", "Java 변수 행성", "변수", "int 변수를 배웁니다.", "int score = 10;", 10)),
                1,
                0));

        ExamLearningScopeService.ExamLearningEligibility eligibility = service.eligibility(2L, 1L, "BRONZE");

        assertTrue(eligibility.eligible());
        assertEquals(0, eligibility.incompleteRequiredLessonCount());
        assertEquals("AI 코딩테스트를 시작할 수 있습니다.", eligibility.message());
    }

    @Test
    void eligibility_blocks_exam_when_required_lessons_are_missing() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(
                List.of(),
                0,
                0));

        ExamLearningScopeService.ExamLearningEligibility eligibility = service.eligibility(2L, 1L, "BRONZE");

        assertEquals(false, eligibility.eligible());
        assertEquals(0, eligibility.incompleteRequiredLessonCount());
        assertEquals("응시 가능한 필수 레슨이 없습니다.", eligibility.message());
    }

    @Test
    void eligibility_blocks_exam_when_required_lessons_are_incomplete() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(
                List.of(item("LESSON", "Java 변수 행성", "변수", "int 변수를 배웁니다.", "int score = 10;", 10)),
                1,
                1));

        ExamLearningScopeService.ExamLearningEligibility eligibility = service.eligibility(2L, 1L, "BRONZE");

        assertEquals(false, eligibility.eligible());
        assertEquals(1, eligibility.incompleteRequiredLessonCount());
        assertEquals("필수 레슨의 이론 학습과 문제풀이를 모두 완료해야 AI 코딩테스트를 시작할 수 있습니다.", eligibility.message());
    }

    private static ExamLearningScopeItem item(
            String sourceType,
            String nodeTitle,
            String title,
            String summary,
            String exampleCode,
            Integer sortOrder
    ) {
        ExamLearningScopeItem item = new ExamLearningScopeItem();
        item.setSourceType(sourceType);
        item.setNodeTitle(nodeTitle);
        item.setTitle(title);
        item.setSummary(summary);
        item.setExampleCode(exampleCode);
        item.setSortOrder(sortOrder);
        return item;
    }

    private record FakeExamLearningScopeMapper(
            List<ExamLearningScopeItem> lessons,
            int requiredLessonCount,
            int incompleteRequiredLessonCount
    ) implements ExamLearningScopeMapper {
        private FakeExamLearningScopeMapper(List<ExamLearningScopeItem> lessons) {
            this(lessons, 0, 0);
        }

        @Override
        public List<ExamLearningScopeItem> findCompletedLessonScope(Long userId, Long subjectId, String levelCode) {
            return lessons;
        }

        @Override
        public int countRequiredLessons(Long subjectId, String levelCode) {
            return requiredLessonCount;
        }

        @Override
        public int countIncompleteRequiredLessons(Long userId, Long subjectId, String levelCode) {
            return incompleteRequiredLessonCount;
        }
    }
}
