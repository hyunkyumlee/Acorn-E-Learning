package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamCodeRunResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.security.SessionUser;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamCodeRunService {
    private final ExamSessionMapper examSessionMapper;
    private final AiExamProblemMapper aiExamProblemMapper;
    private final TestCaseExecutionService testCaseExecutionService;

    public ExamCodeRunService(
            ExamSessionMapper examSessionMapper,
            AiExamProblemMapper aiExamProblemMapper,
            TestCaseExecutionService testCaseExecutionService
    ) {
        this.examSessionMapper = examSessionMapper;
        this.aiExamProblemMapper = aiExamProblemMapper;
        this.testCaseExecutionService = testCaseExecutionService;
    }

    @Transactional(readOnly = true)
    public ExamCodeRunResponse run(SessionUser sessionUser, Long examId, Long aiProblemId, SaveExamAnswerRequest request) {
        Long userId = requireUserId(sessionUser);
        requireSession(userId, examId);
        AiExamProblem problem = aiExamProblemMapper.findByIdAndExamId(aiProblemId, examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험 문제를 찾을 수 없습니다."));
        ExamAnswer answer = new ExamAnswer();
        answer.setExamId(examId);
        answer.setAiProblemId(aiProblemId);
        answer.setAnswerText(request.answerText());

        long startedAt = System.nanoTime();
        TestCaseExecutionResult result = testCaseExecutionService.execute(problem, answer);
        long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        return ExamCodeRunResponse.from(result, elapsedMs);
    }

    private void requireSession(Long userId, Long examId) {
        examSessionMapper.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험을 찾을 수 없습니다."));
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}
