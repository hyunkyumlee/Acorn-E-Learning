package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamSessionResponse;
import com.acorn.elearning.exam.dto.response.ExamSubmitResponse;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.mapper.ExamAnswerMapper;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.security.SessionUser;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiExamServiceStatusTest {

    @Test
    void saveAnswer_blocks_when_exam_is_already_graded() {
        FakeExamSessionMapper sessionMapper = new FakeExamSessionMapper(gradedSession());
        AiExamService service = service(sessionMapper, unusedMapper(AiExamProblemMapper.class), unusedMapper(ExamAnswerMapper.class));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveAnswer(user(), 9L, 99L, new SaveExamAnswerRequest("class Solution {}")));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
        assertEquals("답안을 수정할 수 없는 시험 상태입니다.", exception.getMessage());
    }

    @Test
    void submit_returns_existing_result_when_exam_is_already_graded() {
        FakeExamSessionMapper sessionMapper = new FakeExamSessionMapper(gradedSession());
        AiExamService service = service(sessionMapper, unusedMapper(AiExamProblemMapper.class), unusedMapper(ExamAnswerMapper.class));

        ExamSubmitResponse response = service.submit(user(), 9L);

        assertEquals(9L, response.examId());
        assertEquals(ExamSessionStatusPolicy.GRADED, response.status());
        assertEquals(0, sessionMapper.updateResultCount);
    }

    @Test
    void create_returns_active_exam_without_creating_another_session() {
        ExamSession activeSession = readySession();
        FakeExamSessionMapper sessionMapper = new FakeExamSessionMapper(activeSession);
        sessionMapper.activeSession = activeSession;
        AiExamService service = service(sessionMapper, new EmptyAiExamProblemMapper(), new EmptyExamAnswerMapper());

        ExamSessionResponse response = service.create(user(), new CreateExamRequest(1L, "BEGINNER"));

        assertEquals(8L, response.examId());
        assertEquals(0, sessionMapper.insertCount);
    }

    private static AiExamService service(
            ExamSessionMapper sessionMapper,
            AiExamProblemMapper problemMapper,
            ExamAnswerMapper answerMapper
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        AiRequestLogService logService = new AiRequestLogService(unusedMapper(AiRequestLogMapper.class), objectMapper);
        return new AiExamService(
                sessionMapper,
                problemMapper,
                answerMapper,
                unusedChatGptApiClient(objectMapper),
                logService,
                new TestCaseExecutionService(objectMapper),
                new AiReviewService(unusedChatGptApiClient(objectMapper), logService),
                new ExamLearningScopeService(new LearnedExamLearningScopeMapper()),
                objectMapper);
    }

    private static ExamSession readySession() {
        ExamSession session = baseSession();
        session.setExamId(8L);
        session.setStatus(ExamSessionStatusPolicy.READY);
        return session;
    }

    private static ExamSession gradedSession() {
        ExamSession session = baseSession();
        session.setExamId(9L);
        session.setStatus(ExamSessionStatusPolicy.GRADED);
        session.setResultStatus("PASSED");
        session.setCorrectCount(3);
        return session;
    }

    private static ExamSession baseSession() {
        ExamSession session = new ExamSession();
        session.setUserId(2L);
        session.setSubjectId(1L);
        session.setLevelCode("BEGINNER");
        session.setTotalProblemCount(3);
        session.setCorrectCount(0);
        session.setRetryCount(0);
        return session;
    }

    private static SessionUser user() {
        return new SessionUser(2L, "learner@example.com", "학습자", SessionUser.ROLE_USER, false);
    }

    private static ChatGptApiClient unusedChatGptApiClient(ObjectMapper objectMapper) {
        return new ChatGptApiClient("openai", true, "test-key", "https://example.com", "gpt-test", 1, objectMapper) {
            @Override
            public ChatGptResponse send(ChatGptRequest request) {
                throw new AssertionError("이 테스트에서 ChatGPT API를 호출하면 안 됩니다.");
            }
        };
    }

    private static <T> T unusedMapper(Class<T> type) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (target, method, args) -> {
                    throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                });
        return type.cast(proxy);
    }

    private static class LearnedExamLearningScopeMapper implements ExamLearningScopeMapper {
        @Override
        public List<ExamLearningScopeItem> findCompletedLessonScope(Long userId, Long subjectId, String levelCode) {
            ExamLearningScopeItem item = new ExamLearningScopeItem();
            item.setSourceType("LESSON");
            item.setNodeTitle("Java 변수");
            item.setTitle("변수");
            item.setSummary("int 변수를 배웁니다.");
            item.setExampleCode("int n = 1;");
            item.setSortOrder(1);
            return List.of(item);
        }

        @Override
        public List<ExamLearningScopeItem> findPassedPracticeScope(Long userId, Long subjectId, String levelCode) {
            return List.of();
        }

        @Override
        public int countAvailableScopeItems(Long userId, Long subjectId, String levelCode) {
            return 1;
        }
    }

    private static class EmptyAiExamProblemMapper implements AiExamProblemMapper {
        @Override
        public Optional<AiExamProblem> findById(Long id) { return Optional.empty(); }

        @Override
        public Optional<AiExamProblem> findByIdAndExamId(Long aiProblemId, Long examId) { return Optional.empty(); }

        @Override
        public List<AiExamProblem> findByExamId(Long examId) { return List.of(); }

        @Override
        public List<AiExamProblem> findAll() {
            return List.of();
        }

        @Override
        public int insert(AiExamProblem model) {
            return 0;
        }

        @Override
        public int update(AiExamProblem model) {
            return 0;
        }
    }

    private static class EmptyExamAnswerMapper implements ExamAnswerMapper {
        @Override
        public Optional<ExamAnswer> findById(Long id) { return Optional.empty(); }

        @Override
        public Optional<ExamAnswer> findByExamIdAndProblemId(Long examId, Long aiProblemId) { return Optional.empty(); }

        @Override
        public List<ExamAnswer> findByExamId(Long examId) {
            return List.of();
        }

        @Override
        public List<ExamAnswer> findAll() {
            return List.of();
        }

        @Override
        public int insert(ExamAnswer model) {
            return 0;
        }

        @Override
        public int update(ExamAnswer model) {
            return 0;
        }

        @Override
        public int upsertAnswer(ExamAnswer model) {
            throw new AssertionError("채점 완료 상태에서는 답안을 저장하면 안 됩니다.");
        }

        @Override
        public int updateGradingResult(ExamAnswer model) {
            return 0;
        }
    }

    private static class FakeExamSessionMapper implements ExamSessionMapper {
        private final ExamSession session;
        private ExamSession activeSession;
        private int insertCount;
        private int updateResultCount;

        FakeExamSessionMapper(ExamSession session) {
            this.session = session;
        }

        @Override
        public Optional<ExamSession> findById(Long id) {
            return Optional.ofNullable(session);
        }

        @Override
        public Optional<ExamSession> findByIdAndUserId(Long examId, Long userId) {
            return Optional.ofNullable(session);
        }

        @Override
        public Optional<ExamSession> findLatestByUserId(Long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<ExamSession> findLatestActiveByUserSubjectLevel(Long userId, Long subjectId, String levelCode) {
            return Optional.ofNullable(activeSession);
        }

        @Override
        public List<ExamSession> findByUserId(Long userId) {
            return List.of();
        }

        @Override
        public List<ExamSession> findAll() {
            return List.of();
        }

        @Override
        public int insert(ExamSession model) {
            insertCount++;
            return 1;
        }

        @Override
        public int update(ExamSession model) {
            return 0;
        }

        @Override
        public int updateStatus(ExamSession model) {
            return 0;
        }

        @Override
        public int updateResult(ExamSession model) {
            updateResultCount++;
            return 1;
        }
    }
}
