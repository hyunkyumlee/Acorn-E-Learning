package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamEligibilityResponse;
import com.acorn.elearning.exam.dto.response.ExamResultResponse;
import com.acorn.elearning.exam.dto.response.ExamSessionResponse;
import com.acorn.elearning.exam.dto.response.ExamStatusResponse;
import com.acorn.elearning.exam.dto.response.ExamSubmitResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.mapper.AiExamProblemMapper;
import com.acorn.elearning.exam.mapper.ExamAnswerMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.AiRequestLog;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.service.AiGeneratedProblemParser.GeneratedProblem;
import com.acorn.elearning.exam.service.ExamLearningScopeService.ExamLearningScope;
import com.acorn.elearning.exam.support.ExamStarterCodeResolver;
import com.acorn.elearning.security.SessionUser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiExamService {
    private static final String TARGET_TYPE = "EXAM_SESSION";
    private static final int PROBLEM_COUNT = 3;
    private static final int PASS_COUNT = 2;

    private final ExamSessionMapper examSessionMapper;
    private final AiExamProblemMapper aiExamProblemMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;
    private final TestCaseExecutionService testCaseExecutionService;
    private final AiReviewService aiReviewService;
    private final ExamLearningScopeService examLearningScopeService;
    private final AiGeneratedProblemParser generatedProblemParser;
    private final ObjectMapper objectMapper;

    public AiExamService(
            ExamSessionMapper examSessionMapper,
            AiExamProblemMapper aiExamProblemMapper,
            ExamAnswerMapper examAnswerMapper,
            ChatGptApiClient chatGptApiClient,
            AiRequestLogService aiRequestLogService,
            TestCaseExecutionService testCaseExecutionService,
            AiReviewService aiReviewService,
            ExamLearningScopeService examLearningScopeService,
            ObjectMapper objectMapper
    ) {
        this.examSessionMapper = examSessionMapper;
        this.aiExamProblemMapper = aiExamProblemMapper;
        this.examAnswerMapper = examAnswerMapper;
        this.chatGptApiClient = chatGptApiClient;
        this.aiRequestLogService = aiRequestLogService;
        this.testCaseExecutionService = testCaseExecutionService;
        this.aiReviewService = aiReviewService;
        this.examLearningScopeService = examLearningScopeService;
        this.generatedProblemParser = new AiGeneratedProblemParser(objectMapper);
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ExamEligibilityResponse eligibility(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        return ExamEligibilityResponse.from(
                examSessionMapper.findLatestByUserId(userId).orElse(null),
                examLearningScopeService.eligibility(userId));
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public ExamSessionResponse create(SessionUser sessionUser, CreateExamRequest request) {
        Long userId = requireUserId(sessionUser);
        ExamLearningScope learningScope = examLearningScopeService.build(userId, request.subjectId(), request.levelCode());
        ExamSession activeSession = examSessionMapper.findLatestActiveByUserSubjectLevel(userId, request.subjectId(), request.levelCode()).orElse(null);
        if (activeSession != null) {
            return detail(userId, activeSession.getExamId());
        }
        ExamSession session = new ExamSession();
        session.setUserId(userId);
        session.setSubjectId(request.subjectId());
        session.setLevelCode(request.levelCode());
        session.setStatus(ExamSessionStatusPolicy.CREATED);
        session.setTotalProblemCount(PROBLEM_COUNT);
        session.setCorrectCount(0);
        session.setRetryCount(0);
        session.setStartedAt(LocalDateTime.now());
        examSessionMapper.insert(session);

        ChatGptRequest chatGptRequest = ExamProblemGenerationRequestFactory.create(request, learningScope, PROBLEM_COUNT);
        AiRequestLog log = aiRequestLogService.start(TARGET_TYPE, session.getExamId(), "PROBLEM_GENERATION", chatGptRequest);
        try {
            ChatGptResponse response = chatGptApiClient.send(chatGptRequest);
            saveGeneratedProblems(session.getExamId(), response.content(), learningScope);
            aiRequestLogService.success(log, response);
        } catch (RuntimeException exception) {
            session.setStatus(ExamSessionStatusPolicy.FAILED);
            examSessionMapper.updateStatus(session);
            aiRequestLogService.failed(log, exception);
            throw exception;
        }

        session.setStatus(ExamSessionStatusPolicy.READY);
        examSessionMapper.updateStatus(session);
        return detail(userId, session.getExamId());
    }

    @Transactional
    public ExamSessionResponse saveAnswer(SessionUser sessionUser, Long examId, Long aiProblemId, SaveExamAnswerRequest request) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireSession(userId, examId);
        ExamSessionStatusPolicy.requireEditable(session);
        aiExamProblemMapper.findByIdAndExamId(aiProblemId, examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험 문제를 찾을 수 없습니다."));
        ExamAnswer answer = new ExamAnswer();
        answer.setExamId(examId);
        answer.setAiProblemId(aiProblemId);
        answer.setAnswerText(request.answerText());
        examAnswerMapper.upsertAnswer(answer);
        return detail(userId, examId);
    }

    @Transactional
    public ExamSubmitResponse submit(SessionUser sessionUser, Long examId) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireSession(userId, examId);
        if (ExamSessionStatusPolicy.graded(session)) {
            return ExamSubmitResponse.from(session);
        }
        ExamSessionStatusPolicy.requireSubmittable(session);
        grade(session);
        return ExamSubmitResponse.from(requireSession(userId, examId));
    }

    @Transactional
    public ExamSubmitResponse retryExecution(SessionUser sessionUser, Long examId) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireSession(userId, examId);
        ExamSessionStatusPolicy.requireSubmittable(session);
        session.setRetryCount(session.getRetryCount() + 1);
        examSessionMapper.updateStatus(session);
        grade(session);
        return ExamSubmitResponse.from(requireSession(userId, examId));
    }

    @Transactional(readOnly = true)
    public ExamSessionResponse detail(SessionUser sessionUser, Long examId) {
        return detail(requireUserId(sessionUser), examId);
    }

    @Transactional(readOnly = true)
    public ExamResultResponse result(SessionUser sessionUser, Long examId) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireSession(userId, examId);
        return ExamResultResponse.from(session, examAnswerMapper.findByExamId(examId));
    }

    @Transactional(readOnly = true)
    public ExamStatusResponse status(SessionUser sessionUser, Long examId) {
        return ExamStatusResponse.from(requireSession(requireUserId(sessionUser), examId));
    }

    private ExamSessionResponse detail(Long userId, Long examId) {
        ExamSession session = requireSession(userId, examId);
        return ExamSessionResponse.from(
                session,
                aiExamProblemMapper.findByExamId(examId),
                examAnswerMapper.findByExamId(examId));
    }

    private void grade(ExamSession session) {
        List<AiExamProblem> problems = aiExamProblemMapper.findByExamId(session.getExamId());
        Map<Long, ExamAnswer> answers = examAnswerMapper.findByExamId(session.getExamId()).stream()
                .collect(Collectors.toMap(ExamAnswer::getAiProblemId, Function.identity()));
        if (problems.size() < PROBLEM_COUNT || answers.size() < PROBLEM_COUNT) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "모든 문제의 답안을 제출해야 합니다.");
        }

        int correctCount = 0;
        for (AiExamProblem problem : problems) {
            ExamAnswer answer = answers.get(problem.getAiProblemId());
            TestCaseExecutionResult executionResult = testCaseExecutionService.execute(problem, answer);
            answer.setPassedCaseCount(executionResult.passedCount());
            answer.setIsCorrect(executionResult.passed());
            answer.setTestCaseResult(toJson(executionResult));
            answer.setAiReview(aiReviewService.reviewAnswer(
                    answer.getAnswerId(),
                    problem.getPrompt(),
                    ExamStarterCodeResolver.starterCode(problem),
                    answer.getAnswerText(),
                    executionResult));
            examAnswerMapper.updateGradingResult(answer);
            if (executionResult.passed()) {
                correctCount++;
            }
        }

        session.setStatus(ExamSessionStatusPolicy.GRADED);
        session.setResultStatus(correctCount >= PASS_COUNT ? "PASSED" : "FAILED");
        session.setCorrectCount(correctCount);
        session.setSubmittedAt(LocalDateTime.now());
        session.setGradedAt(LocalDateTime.now());
        examSessionMapper.updateResult(session);
    }

    private void saveGeneratedProblems(Long examId, String content, ExamLearningScope learningScope) {
        List<GeneratedProblem> generatedProblems = generatedProblemParser.parse(content, learningScope, PROBLEM_COUNT);
        for (int index = 0; index < generatedProblems.size(); index++) {
            GeneratedProblem generatedProblem = generatedProblems.get(index);
            AiExamProblem problem = new AiExamProblem();
            problem.setExamId(examId);
            problem.setProblemNo(index + 1);
            problem.setPrompt(generatedProblem.prompt());
            problem.setTestCaseSpec(generatedProblem.testCaseSpec());
            problem.setAiRawResponse(generatedProblem.rawResponse());
            problem.setStatus("GENERATED");
            aiExamProblemMapper.insert(problem);
        }
    }

    private ExamSession requireSession(Long userId, Long examId) {
        return examSessionMapper.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험을 찾을 수 없습니다."));
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        if (!sessionUser.user()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "AI 코딩테스트는 학습자 계정으로만 이용할 수 있습니다.");
        }
        return sessionUser.userId();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트케이스 실행 결과를 저장할 수 없습니다.");
        }
    }
}
