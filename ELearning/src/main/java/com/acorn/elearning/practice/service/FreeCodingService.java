package com.acorn.elearning.practice.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.service.TestCaseExecutionService;
import com.acorn.elearning.practice.dto.request.FreeCodingRunRequest;
import com.acorn.elearning.practice.dto.response.FreeCodingRunResponse;
import com.acorn.elearning.security.SessionUser;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class FreeCodingService {
    private final TestCaseExecutionService testCaseExecutionService;

    public FreeCodingService(TestCaseExecutionService testCaseExecutionService) {
        this.testCaseExecutionService = testCaseExecutionService;
    }

    public FreeCodingRunResponse run(SessionUser sessionUser, FreeCodingRunRequest request) {
        requireLearner(sessionUser);

        long startedAt = System.nanoTime();
        TestCaseExecutionService.CodeExecutionResult result = testCaseExecutionService.executeRaw(request.source(), request.input());
        long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        return FreeCodingRunResponse.from(result, elapsedMs);
    }

    public void requireLearner(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        if (!sessionUser.user()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "자유 코딩은 학습자 계정으로만 이용할 수 있습니다.");
        }
    }
}
