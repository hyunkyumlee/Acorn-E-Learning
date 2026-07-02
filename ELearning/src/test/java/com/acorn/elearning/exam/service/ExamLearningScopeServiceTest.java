package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExamLearningScopeServiceTest {

    @Test
    void build_returns_learned_lesson_and_practice_scope() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(
                List.of(item("LESSON", "Java 반복문 행성", "반복문", "for 문으로 반복 실행합니다.", "for (int i = 0; i < 3; i++) {}", 100)),
                List.of(item("PRACTICE", "Java 반복문 행성", "합 구하기", "sum += i;", "sum += i;", 101))));

        ExamLearningScopeService.ExamLearningScope scope = service.build(2L, 1L, "BRONZE");

        assertEquals(2, scope.learnedItems().size());
        assertTrue(scope.allowedConcepts().contains("for loop"));
        assertTrue(scope.starterCodePolicy().contains("BufferedReader"));
    }

    @Test
    void build_throws_validation_error_when_scope_is_empty() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(List.of(), List.of()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.build(2L, 1L, "BRONZE"));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
    }

    @Test
    void eligibility_allows_exam_when_user_has_available_scope() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(
                List.of(item("LESSON", "Java 변수 행성", "변수", "int 변수를 배웁니다.", "int score = 10;", 10)),
                List.of()));

        ExamLearningScopeService.ExamLearningEligibility eligibility = service.eligibility(2L);

        assertTrue(eligibility.eligible());
        assertEquals(1, eligibility.availableScopeCount());
    }

    @Test
    void eligibility_blocks_exam_when_user_has_no_available_scope() {
        ExamLearningScopeService service = new ExamLearningScopeService(new FakeExamLearningScopeMapper(List.of(), List.of()));

        ExamLearningScopeService.ExamLearningEligibility eligibility = service.eligibility(2L);

        assertEquals(false, eligibility.eligible());
        assertEquals(0, eligibility.availableScopeCount());
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
            List<ExamLearningScopeItem> practices
    ) implements ExamLearningScopeMapper {
        @Override
        public List<ExamLearningScopeItem> findCompletedLessonScope(Long userId, Long subjectId, String levelCode) {
            return lessons;
        }

        @Override
        public List<ExamLearningScopeItem> findPassedPracticeScope(Long userId, Long subjectId, String levelCode) {
            return practices;
        }

        @Override
        public int countAvailableScopeItems(Long userId, Long subjectId, String levelCode) {
            return lessons.size() + practices.size();
        }
    }
}
