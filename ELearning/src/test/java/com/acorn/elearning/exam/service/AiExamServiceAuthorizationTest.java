package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.mapper.ExamAnswerMapper;
import com.acorn.elearning.exam.mapper.ExamLearningScopeMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.security.SessionUser;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiExamServiceAuthorizationTest {

    @Test
    void eligibility_rejects_admin_before_exam_lookup() {
        AiExamService service = service();

        BusinessException exception = assertThrows(BusinessException.class, () -> service.eligibility(admin()));

        assertEquals(ErrorCode.AUTH_FORBIDDEN, exception.errorCode());
        assertEquals("AI 코딩테스트는 학습자 계정으로만 이용할 수 있습니다.", exception.getMessage());
    }

    private static AiExamService service() {
        ObjectMapper objectMapper = new ObjectMapper();
        AiRequestLogService logService = new AiRequestLogService(unusedMapper(AiRequestLogMapper.class), objectMapper);
        return new AiExamService(
                unusedMapper(ExamSessionMapper.class),
                unusedMapper(AiExamProblemMapper.class),
                unusedMapper(ExamAnswerMapper.class),
                unusedChatGptApiClient(objectMapper),
                logService,
                new TestCaseExecutionService(objectMapper),
                new AiReviewService(unusedChatGptApiClient(objectMapper), logService),
                new ExamLearningScopeService(unusedMapper(ExamLearningScopeMapper.class)),
                objectMapper);
    }

    private static SessionUser admin() {
        return new SessionUser(1L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false);
    }

    private static ChatGptApiClient unusedChatGptApiClient(ObjectMapper objectMapper) {
        return new ChatGptApiClient("openai", true, "test-key", "https://example.com", "gpt-test", 1, objectMapper) {
            @Override
            public ChatGptResponse send(ChatGptRequest request) {
                throw new AssertionError("권한 실패 시 ChatGPT API를 호출하면 안 됩니다.");
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
}
