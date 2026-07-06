package com.acorn.elearning.learning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * ProgressService.markPracticePassed 단위 테스트.
 * 문제풀이 세트 통과 시 learning_progress upsert 동작 검증.
 * 손수 만든 test double 사용(Mockito 미사용).
 */
class ProgressServiceMarkPracticePassedTest {

    private static final long USER_ID = 2L;
    private static final long SUBJECT_ID = 10L;
    private static final long NODE_ID = 100L;

    @Test
    void insert_when_no_progress_row_yet() {
        RecordingProgressMapper progressMapper = new RecordingProgressMapper(null);
        ProgressService service = new ProgressService(progressMapper, nodeMapper(SUBJECT_ID));

        service.markPracticePassed(USER_ID, SUBJECT_ID, NODE_ID);

        // 진행 행이 없던 단원 → insert. 문제풀이만 통과라 아직 완전완료 아님(50%, completedAt=null).
        assertEquals(1, progressMapper.insertCount);
        assertEquals(0, progressMapper.updateCount);
        LearningProgress written = progressMapper.lastWritten;
        assertNotNull(written);
        assertTrue(written.getPracticePassed());
        assertFalse(written.getLessonCompleted());
        assertEquals(0, new BigDecimal("50.00").compareTo(written.getProgressRate()));
        assertNull(written.getCompletedAt());
    }

    @Test
    void update_to_full_when_lesson_already_completed() {
        LearningProgress existing = row(true, false, new BigDecimal("50.00"));
        RecordingProgressMapper progressMapper = new RecordingProgressMapper(existing);
        ProgressService service = new ProgressService(progressMapper, nodeMapper(SUBJECT_ID));

        service.markPracticePassed(USER_ID, SUBJECT_ID, NODE_ID);

        // 이론이 이미 완료된 단원 → 둘 다 완료 = 100% + completedAt 기록.
        assertEquals(0, progressMapper.insertCount);
        assertEquals(1, progressMapper.updateCount);
        LearningProgress written = progressMapper.lastWritten;
        assertTrue(written.getPracticePassed());
        assertEquals(0, new BigDecimal("100.00").compareTo(written.getProgressRate()));
        assertNotNull(written.getCompletedAt());
    }

    @Test
    void idempotent_noop_when_already_practice_passed() {
        LearningProgress existing = row(true, true, new BigDecimal("100.00"));
        RecordingProgressMapper progressMapper = new RecordingProgressMapper(existing);
        ProgressService service = new ProgressService(progressMapper, nodeMapper(SUBJECT_ID));

        service.markPracticePassed(USER_ID, SUBJECT_ID, NODE_ID);

        // 이미 통과 처리된 단원 → 아무 write도 하지 않음(중복 호출에도 completedAt 덮어쓰기 방지).
        assertEquals(0, progressMapper.insertCount);
        assertEquals(0, progressMapper.updateCount);
    }

    @Test
    void throws_not_found_when_node_missing() {
        RecordingProgressMapper progressMapper = new RecordingProgressMapper(null);
        ProgressService service = new ProgressService(progressMapper, nodeMapper(null));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.markPracticePassed(USER_ID, SUBJECT_ID, NODE_ID));

        assertEquals(ErrorCode.COMMON_NOT_FOUND, ex.errorCode());
        assertEquals(0, progressMapper.insertCount);
        assertEquals(0, progressMapper.updateCount);
    }

    @Test
    void throws_validation_when_node_subject_mismatch() {
        RecordingProgressMapper progressMapper = new RecordingProgressMapper(null);
        // 넘겨받은 SUBJECT_ID(10)와 다른 과목(999)에 속한 단원.
        ProgressService service = new ProgressService(progressMapper, nodeMapper(999L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.markPracticePassed(USER_ID, SUBJECT_ID, NODE_ID));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, ex.errorCode());
        assertEquals(0, progressMapper.insertCount);
        assertEquals(0, progressMapper.updateCount);
    }

    // --- test doubles ---

    private static LearningProgress row(boolean lessonCompleted, boolean practicePassed, BigDecimal rate) {
        LearningProgress row = new LearningProgress();
        row.setProgressId(1L);
        row.setUserId(USER_ID);
        row.setSubjectId(SUBJECT_ID);
        row.setNodeId(NODE_ID);
        row.setLessonCompleted(lessonCompleted);
        row.setPracticePassed(practicePassed);
        row.setProgressRate(rate);
        return row;
    }

    /** findById가 지정 과목의 단원을 돌려주는 CurriculumNodeMapper. subjectId=null이면 단원 없음(Optional.empty). */
    private static CurriculumNodeMapper nodeMapper(Long nodeSubjectId) {
        return (CurriculumNodeMapper) Proxy.newProxyInstance(
                CurriculumNodeMapper.class.getClassLoader(),
                new Class<?>[]{CurriculumNodeMapper.class},
                (target, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        if (nodeSubjectId == null) {
                            return Optional.empty();
                        }
                        CurriculumNode node = new CurriculumNode();
                        node.setNodeId(NODE_ID);
                        node.setSubjectId(nodeSubjectId);
                        return Optional.of(node);
                    }
                    throw new AssertionError(method.getName() + " 는 호출되면 안 됩니다.");
                });
    }

    /** insert/update 호출을 세고 마지막으로 write된 행을 잡아두는 LearningProgressMapper. */
    private static class RecordingProgressMapper implements LearningProgressMapper {
        private final LearningProgress existing;
        private int insertCount;
        private int updateCount;
        private LearningProgress lastWritten;

        private RecordingProgressMapper(LearningProgress existing) {
            this.existing = existing;
        }

        @Override
        public Optional<LearningProgress> findByUserSubjectNode(Long userId, Long subjectId, Long nodeId) {
            return Optional.ofNullable(existing);
        }

        @Override
        public int insert(LearningProgress model) {
            insertCount++;
            lastWritten = model;
            return 1;
        }

        @Override
        public int update(LearningProgress model) {
            updateCount++;
            lastWritten = model;
            return 1;
        }

        @Override
        public Optional<LearningProgress> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<LearningProgress> findAll() {
            return List.of();
        }

        @Override
        public List<LearningProgress> findByUserIdAndSubjectId(Long userId, Long subjectId) {
            return List.of();
        }
    }
}
