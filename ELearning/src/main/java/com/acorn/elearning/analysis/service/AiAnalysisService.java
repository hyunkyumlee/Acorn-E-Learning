package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisStatusResponse;
import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.analysis.mapper.AnalysisDashboardMapper;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import com.acorn.elearning.analysis.model.AiAnalysisReport;
import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.ExamAnswerMapper;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.AiRequestLog;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.service.AiRequestLogService;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private static final String TARGET_TYPE = "ANALYSIS_REPORT";
    private static final int TREND_LIMIT = 7;

    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final AnalysisDashboardMapper analysisDashboardMapper;
    private final ExamSessionMapper examSessionMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final PaymentAccessService paymentAccessService;
    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(
            AiAnalysisReportMapper aiAnalysisReportMapper,
            AnalysisDashboardMapper analysisDashboardMapper,
            ExamSessionMapper examSessionMapper,
            ExamAnswerMapper examAnswerMapper,
            PaymentAccessService paymentAccessService,
            ChatGptApiClient chatGptApiClient,
            AiRequestLogService aiRequestLogService,
            ObjectMapper objectMapper
    ) {
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
        this.analysisDashboardMapper = analysisDashboardMapper;
        this.examSessionMapper = examSessionMapper;
        this.examAnswerMapper = examAnswerMapper;
        this.paymentAccessService = paymentAccessService;
        this.chatGptApiClient = chatGptApiClient;
        this.aiRequestLogService = aiRequestLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AnalysisReportResponse latest(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        return aiAnalysisReportMapper.findByUserId(userId).stream()
                .findFirst()
                .map(AnalysisReportResponse::from)
                .orElse(null);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void refreshLatestIfRequired(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        AnalysisExamSummary latestExam = analysisDashboardMapper.findLatestGradedExamSummary(userId).orElse(null);
        if (latestExam == null || latestExam.getExamId() == null) {
            return;
        }
        refreshReportIfRequired(userId, latestExam.getExamId());
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AnalysisReportResponse generate(SessionUser sessionUser, GenerateAnalysisRequest request) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireExam(userId, request.examId());
        AiAnalysisReport report = new AiAnalysisReport();
        report.setUserId(userId);
        report.setExamId(session.getExamId());
        report.setStatus("PENDING");
        report.setRetryCount(0);
        aiAnalysisReportMapper.insert(report);
        generateContent(report, session);
        return AnalysisReportResponse.from(report);
    }

    private void refreshReportIfRequired(Long userId, Long examId) {
        AiAnalysisReport report = aiAnalysisReportMapper.findByExamIdAndUserId(examId, userId).orElse(null);
        if (!needsAutoRefresh(report)) {
            return;
        }
        ExamSession session = requireExam(userId, examId);
        try {
            if (report == null) {
                createAutoReport(userId, session);
                return;
            }
            retryAutoReport(report, session);
        } catch (BusinessException exception) {
            log.warn("AI 분석 자동 갱신에 실패했습니다. examId={}", examId, exception);
        }
    }

    private boolean needsAutoRefresh(AiAnalysisReport report) {
        if (report == null) {
            return true;
        }
        if ("SUCCESS".equals(report.getStatus()) || "PENDING".equals(report.getStatus())) {
            return false;
        }
        return "FAILED".equals(report.getStatus()) && number(report.getRetryCount()) < 1;
    }

    private void createAutoReport(Long userId, ExamSession session) {
        AiAnalysisReport report = new AiAnalysisReport();
        report.setUserId(userId);
        report.setExamId(session.getExamId());
        report.setStatus("PENDING");
        report.setRetryCount(0);
        aiAnalysisReportMapper.insert(report);
        generateContent(report, session);
    }

    private void retryAutoReport(AiAnalysisReport report, ExamSession session) {
        report.setStatus("PENDING");
        report.setRetryCount(number(report.getRetryCount()) + 1);
        report.setAnalysisErrorCode(null);
        aiAnalysisReportMapper.update(report);
        generateContent(report, session);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AnalysisReportResponse retry(SessionUser sessionUser, Long reportId) {
        Long userId = requireUserId(sessionUser);
        AiAnalysisReport report = requireReport(userId, reportId);
        ExamSession session = requireExam(userId, report.getExamId());
        report.setStatus("PENDING");
        report.setRetryCount(number(report.getRetryCount()) + 1);
        report.setAnalysisErrorCode(null);
        aiAnalysisReportMapper.update(report);
        generateContent(report, session);
        return AnalysisReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public AnalysisReportResponse detail(SessionUser sessionUser, Long reportId) {
        return AnalysisReportResponse.from(requireReport(requireUserId(sessionUser), reportId));
    }

    @Transactional(readOnly = true)
    public AnalysisStatusResponse status(SessionUser sessionUser, Long reportId) {
        return AnalysisStatusResponse.from(requireReport(requireUserId(sessionUser), reportId));
    }

    private void generateContent(AiAnalysisReport report, ExamSession session) {
        boolean premiumActive = paymentAccessService.hasPremiumAccess(report.getUserId());
        ChatGptRequest request = analysisRequest(session, premiumActive);
        AiRequestLog log = aiRequestLogService.start(TARGET_TYPE, report.getReportId(), "ANALYSIS_GENERATION", request);
        try {
            ChatGptResponse response = chatGptApiClient.send(request);
            applyAnalysisResponse(report, response.content(), premiumActive);
            aiAnalysisReportMapper.update(report);
            aiRequestLogService.success(log, response);
        } catch (RuntimeException exception) {
            report.setStatus("FAILED");
            report.setAnalysisErrorCode(exception instanceof BusinessException businessException
                    ? businessException.errorCode().code()
                    : ErrorCode.COMMON_INTERNAL_ERROR.code());
            aiAnalysisReportMapper.update(report);
            aiRequestLogService.failed(log, exception);
            throw exception;
        }
    }

    private ChatGptRequest analysisRequest(ExamSession session, boolean premiumActive) {
        Long userId = session.getUserId();
        Long subjectId = session.getSubjectId();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("instruction", """
                한국어 존댓말로 누적 학습 분석을 JSON으로 작성해 주세요.
                필드는 freeSummary, premiumDetail을 사용해 주세요.
                freeSummary는 2문장 이내로 작성하고, 문장 끝은 '~입니다', '~합니다'처럼 존댓말로 작성해 주세요.
                premiumDetail은 strengths, weaknesses, nextActions 배열을 포함해 주세요.
                전체 누적 데이터를 기준으로 분석하고, '시험' 대신 '코딩 테스트'라는 표현을 사용해 주세요.
                """);
        payload.put("premiumActive", premiumActive);
        payload.put("targetCodingTest", session);
        payload.put("targetAnswers", examAnswerMapper.findByExamId(session.getExamId()));
        payload.put("codingTestAggregate", analysisDashboardMapper.findCodingExamAggregate(userId, subjectId)
                .orElseGet(AnalysisCodingExamAggregate::new));
        payload.put("recentCodingTests", analysisDashboardMapper.findRecentGradedExamSummaries(userId, subjectId, TREND_LIMIT));
        payload.put("codingMistakeStats", analysisDashboardMapper.findCodingMistakeStats(userId, subjectId));
        payload.put("learningProgress", analysisDashboardMapper.findLearningProgressStats(userId, subjectId));
        payload.put("practiceSummary", analysisDashboardMapper.findPracticeSummary(userId, subjectId)
                .orElseGet(AnalysisPracticeSummary::new));
        payload.put("wrongAnswerSummary", analysisDashboardMapper.findWrongAnswerSummary(userId, subjectId)
                .orElseGet(AnalysisWrongAnswerSummary::new));
        payload.put("weakNodes", analysisDashboardMapper.findWrongAnswerNodeStats(userId, subjectId));
        return new ChatGptRequest(
                "exam-analysis",
                "analysis-v2",
                payload);
    }

    private void applyAnalysisResponse(AiAnalysisReport report, String content, boolean premiumActive) {
        try {
            JsonNode root = objectMapper.readTree(content);
            report.setStatus("SUCCESS");
            report.setFreeSummary(requiredText(root, "freeSummary"));
            report.setPremiumDetail(premiumActive && !root.path("premiumDetail").isMissingNode()
                    ? objectMapper.writeValueAsString(root.path("premiumDetail"))
                    : null);
            report.setAnalysisErrorCode(null);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI 분석 결과 형식이 올바르지 않습니다.");
        }
    }

    private String requiredText(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (!field.isTextual() || field.asText().isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI 분석 요약이 비어 있습니다.");
        }
        return field.asText();
    }

    private ExamSession requireExam(Long userId, Long examId) {
        return examSessionMapper.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 코딩 테스트를 찾을 수 없습니다."));
    }

    private AiAnalysisReport requireReport(Long userId, Long reportId) {
        return aiAnalysisReportMapper.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 분석 결과를 찾을 수 없습니다."));
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private int number(Integer value) {
        return value == null ? 0 : value;
    }
}
