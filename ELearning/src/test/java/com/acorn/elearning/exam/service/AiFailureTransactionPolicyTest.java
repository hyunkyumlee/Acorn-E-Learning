package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.service.AiAnalysisService;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.model.AiRequestLog;
import com.acorn.elearning.security.SessionUser;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class AiFailureTransactionPolicyTest {

    @Test
    void request_log_writes_use_requires_new_transactions() throws Exception {
        assertRequiresNew(AiRequestLogService.class.getMethod(
                "start", String.class, Long.class, String.class, ChatGptRequest.class));
        assertRequiresNew(AiRequestLogService.class.getMethod(
                "success", AiRequestLog.class, ChatGptResponse.class));
        assertRequiresNew(AiRequestLogService.class.getMethod(
                "failed", AiRequestLog.class, Exception.class));
    }

    @Test
    void ai_generation_methods_commit_business_failure_status() throws Exception {
        assertCommitsBusinessFailure(AiExamService.class.getMethod(
                "create", SessionUser.class, CreateExamRequest.class));
        assertCommitsBusinessFailure(AiAnalysisService.class.getMethod(
                "generate", SessionUser.class, GenerateAnalysisRequest.class));
        assertCommitsBusinessFailure(AiAnalysisService.class.getMethod(
                "retry", SessionUser.class, Long.class));
    }

    private void assertRequiresNew(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
    }

    private void assertCommitsBusinessFailure(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(Arrays.asList(transactional.noRollbackFor()).contains(BusinessException.class));
    }
}
