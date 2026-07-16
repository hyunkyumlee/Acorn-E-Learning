package com.acorn.elearning.practice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.service.TestCaseExecutionService;
import com.acorn.elearning.practice.dto.request.FreeCodingRunRequest;
import com.acorn.elearning.security.SessionUser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class FreeCodingServiceAuthorizationTest {

    @Test
    void run_rejects_admin_before_code_execution() {
        FreeCodingService service = new FreeCodingService(new TestCaseExecutionService(new ObjectMapper()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.run(admin(), new FreeCodingRunRequest("public class Solution {}", "")));

        assertEquals(ErrorCode.AUTH_FORBIDDEN, exception.errorCode());
        assertEquals("자유 코딩은 학습자 계정으로만 이용할 수 있습니다.", exception.getMessage());
    }

    private static SessionUser admin() {
        return new SessionUser(1L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false);
    }
}
