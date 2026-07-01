package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.security.SessionUser;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ExamCodeRunServiceAuthorizationTest {

    @Test
    void run_rejects_admin_before_exam_lookup() {
        ExamCodeRunService service = new ExamCodeRunService(
                unusedMapper(ExamSessionMapper.class),
                unusedMapper(AiExamProblemMapper.class),
                new TestCaseExecutionService(new ObjectMapper()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.run(admin(), 1L, 1L, new SaveExamAnswerRequest("class Solution {}")));

        assertEquals(ErrorCode.AUTH_FORBIDDEN, exception.errorCode());
        assertEquals("AI 코딩테스트는 학습자 계정으로만 이용할 수 있습니다.", exception.getMessage());
    }

    private static SessionUser admin() {
        return new SessionUser(1L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false);
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
