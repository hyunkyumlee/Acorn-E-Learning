package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.mapper.ExamAnswerMapper;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.security.SessionUser;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiExamServiceGateTest {

    @Test
    void create_blocks_before_session_insert_when_learning_scope_is_empty() {
        ObjectMapper objectMapper = new ObjectMapper();
        CountingExamSessionMapper examSessionMapper = new CountingExamSessionMapper();
        AiExamService service = new AiExamService(
                examSessionMapper,
                unusedMapper(AiExamProblemMapper.class),
                unusedMapper(ExamAnswerMapper.class),
                unusedChatGptApiClient(objectMapper),
                new AiRequestLogService(unusedMapper(AiRequestLogMapper.class), objectMapper),
                new TestCaseExecutionService(objectMapper),
                new AiReviewService(unusedChatGptApiClient(objectMapper), new AiRequestLogService(unusedMapper(AiRequestLogMapper.class), objectMapper)),
                new ExamLearningScopeService(new EmptyExamLearningScopeMapper()),
                objectMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(user(), new CreateExamRequest(1L, "BRONZE")));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
        assertEquals(0, examSessionMapper.insertCount);
    }

    private static SessionUser user() {
        return new SessionUser(2L, "learner@example.com", "학습자", SessionUser.ROLE_USER, false);
    }

    private static ChatGptApiClient unusedChatGptApiClient(ObjectMapper objectMapper) {
        return new ChatGptApiClient("openai", true, "test-key", "https://example.com", "gpt-test", 1, objectMapper) {
            @Override
            public ChatGptResponse send(ChatGptRequest request) {
                throw new AssertionError("gate 실패 시 ChatGPT API를 호출하면 안 됩니다.");
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

    private static class EmptyExamLearningScopeMapper implements ExamLearningScopeMapper {
        @Override
        public List<ExamLearningScopeItem> findCompletedLessonScope(Long userId, Long subjectId, String levelCode) {
            return List.of();
        }

        @Override
        public List<ExamLearningScopeItem> findPassedPracticeScope(Long userId, Long subjectId, String levelCode) {
            return List.of();
        }

        @Override
        public int countAvailableScopeItems(Long userId, Long subjectId, String levelCode) {
            return 0;
        }
    }

    private static class CountingExamSessionMapper implements ExamSessionMapper {
        private int insertCount;

        @Override
        public Optional<ExamSession> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<ExamSession> findByIdAndUserId(Long examId, Long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<ExamSession> findLatestByUserId(Long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<ExamSession> findLatestActiveByUserSubjectLevel(Long userId, Long subjectId, String levelCode) {
            return Optional.empty();
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
            return 0;
        }
    }
}
