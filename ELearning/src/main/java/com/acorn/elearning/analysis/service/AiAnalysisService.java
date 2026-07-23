package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisAutoRefreshResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisStatusResponse;
import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.analysis.mapper.AnalysisDashboardMapper;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisCodingAnswerSummary;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AiAnalysisReport;
import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.AiRequestLog;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.service.AiRequestLogService;
import com.acorn.elearning.exam.support.ExamStarterCodeResolver;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private static final String TARGET_TYPE = "ANALYSIS_REPORT";
    private static final int TREND_LIMIT = 7;
    private static final int REPORT_LOCK_STRIPES = 64;

    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final AnalysisDashboardMapper analysisDashboardMapper;
    private final ExamSessionMapper examSessionMapper;
    private final PaymentAccessService paymentAccessService;
    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;
    private final ObjectMapper objectMapper;
    private final Object[] reportLocks = initializeReportLocks();

    public AiAnalysisService(
            AiAnalysisReportMapper aiAnalysisReportMapper,
            AnalysisDashboardMapper analysisDashboardMapper,
            ExamSessionMapper examSessionMapper,
            PaymentAccessService paymentAccessService,
            ChatGptApiClient chatGptApiClient,
            AiRequestLogService aiRequestLogService,
            ObjectMapper objectMapper
    ) {
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
        this.analysisDashboardMapper = analysisDashboardMapper;
        this.examSessionMapper = examSessionMapper;
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
                .map(report -> responseFor(userId, report))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean latestRefreshRequired(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        return analysisDashboardMapper.findLatestGradedExamSummary(userId)
                .map(AnalysisExamSummary::getExamId)
                .map(examId -> aiAnalysisReportMapper.findByExamIdAndUserId(examId, userId).isEmpty())
                .orElse(false);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AnalysisAutoRefreshResponse refreshLatestIfRequired(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        return analysisDashboardMapper.findLatestGradedExamSummary(userId)
                .map(AnalysisExamSummary::getExamId)
                .map(examId -> refreshReportIfRequired(userId, examId))
                .orElseGet(() -> AnalysisAutoRefreshResponse.skipped(null));
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AnalysisReportResponse generate(SessionUser sessionUser, GenerateAnalysisRequest request) {
        Long userId = requireUserId(sessionUser);
        ExamSession session = requireExam(userId, request.examId());
        synchronized (reportLock(userId, session.getExamId())) {
            AiAnalysisReport existingReport = aiAnalysisReportMapper.findByExamIdAndUserId(session.getExamId(), userId)
                    .orElse(null);
            if (existingReport != null) {
                return responseFor(userId, existingReport);
            }
            ReportClaim claim = pendingReport(userId, session);
            if (!claim.created()) {
                return responseFor(userId, claim.report());
            }
            generateContent(claim.report(), session);
            return responseFor(userId, claim.report());
        }
    }

    private AnalysisAutoRefreshResponse refreshReportIfRequired(Long userId, Long examId) {
        synchronized (reportLock(userId, examId)) {
            AiAnalysisReport report = aiAnalysisReportMapper.findByExamIdAndUserId(examId, userId).orElse(null);
            if (report != null) {
                return AnalysisAutoRefreshResponse.skipped(
                        responseFor(userId, report)
                );
            }
            ExamSession session = requireExam(userId, examId);
            ReportClaim claim = pendingReport(userId, session);
            if (!claim.created()) {
                return AnalysisAutoRefreshResponse.skipped(responseFor(userId, claim.report()));
            }
            try {
                generateContent(claim.report(), session);
            } catch (RuntimeException exception) {
                log.warn("AI 분석 자동 갱신에 실패했습니다. examId={}", examId, exception);
            }
            return AnalysisAutoRefreshResponse.attempted(responseFor(userId, claim.report()));
        }
    }

    private ReportClaim pendingReport(Long userId, ExamSession session) {
        AiAnalysisReport report = new AiAnalysisReport();
        report.setUserId(userId);
        report.setExamId(session.getExamId());
        report.setStatus("PENDING");
        report.setRetryCount(0);
        try {
            aiAnalysisReportMapper.insert(report);
            return new ReportClaim(report, true);
        } catch (DuplicateKeyException exception) {
            AiAnalysisReport existingReport = aiAnalysisReportMapper
                    .findByExamIdAndUserIdForUpdate(session.getExamId(), userId)
                    .orElseThrow(() -> exception);
            return new ReportClaim(existingReport, false);
        }
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AnalysisReportResponse retry(SessionUser sessionUser, Long reportId) {
        Long userId = requireUserId(sessionUser);
        AiAnalysisReport initialReport = requireReport(userId, reportId);
        if (!"FAILED".equals(initialReport.getStatus())) {
            return responseFor(userId, initialReport);
        }
        int expectedRetryCount = number(initialReport.getRetryCount());
        synchronized (reportLock(userId, initialReport.getExamId())) {
            AiAnalysisReport report = requireReport(userId, reportId);
            if (!"FAILED".equals(report.getStatus())) {
                return responseFor(userId, report);
            }
            ExamSession session = requireExam(userId, report.getExamId());
            if (aiAnalysisReportMapper.claimRetry(reportId, userId, expectedRetryCount) == 0) {
                AiAnalysisReport latestReport = aiAnalysisReportMapper
                        .findByExamIdAndUserIdForUpdate(report.getExamId(), userId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.COMMON_NOT_FOUND,
                                "AI 분석 결과를 찾을 수 없습니다."
                        ));

                return responseFor(userId, latestReport);
            }
            AiAnalysisReport claimedReport = requireReport(userId, reportId);
            generateContent(claimedReport, session);
            return responseFor(userId, claimedReport);
        }
    }

    @Transactional(readOnly = true)
    public AnalysisReportResponse detail(SessionUser sessionUser, Long reportId) {
        Long userId = requireUserId(sessionUser);
        AiAnalysisReport report = requireReport(userId, reportId);

        return responseFor(userId, report);
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
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("instruction", """
                한국어 존댓말로 전체 누적 코딩테스트 분석을 JSON으로 작성해 주세요.
                필드는 freeSummary, premiumDetail을 사용해 주세요.
                freeSummary는 2문장 이내로 작성하고, 문장 끝은 '~입니다', '~합니다'처럼 존댓말로 작성해 주세요.
                premiumDetail은 strengths, weaknesses, nextActions 배열만 포함해 주세요.
                premiumDetail의 각 배열 항목은 화면 bullet로 표시되므로 짧은 관찰 문장으로 작성하고 코드블록은 넣지 마세요.
                사용자가 지금까지 채점 완료한 모든 코딩테스트 데이터를 기준으로 분석해 주세요.
                학습 진행, 일반 문제 풀이, 일반 오답노트는 분석 근거로 사용하지 마세요.
                codingAnswerSummaries에는 starterCode와 submittedCode가 함께 포함됩니다.
                starterCode는 시스템이 제공한 기본 구조이고, submittedCode가 사용자가 제출한 코드입니다.
                starterCode와 submittedCode를 비교해 사용자가 추가하거나 바꾼 풀이 로직만 강점·약점 근거로 삼아 주세요.
                사용자가 작성하지 않은 기본 코드 구조나 입력 처리 틀을 사용자의 강점으로 칭찬하지 마세요.
                strengths는 정답 처리된 제출, 테스트케이스 통과 기록, 사용자가 직접 작성한 조건/반복/계산/출력 로직이 안정적일 때만 작성해 주세요.
                입력값 읽기, 변수 선언, 배열 선언, import, Scanner, class/main 구조, close 호출, TODO 주석 제거 여부는 starterCode에 포함될 수 있으므로 strengths에 넣지 마세요.
                correct가 false이거나 passedCaseCount가 0인 답안은 강점이 아니라 weaknesses 또는 nextActions의 근거로만 사용해 주세요.
                submittedCode가 starterCode와 거의 같거나 TODO 주변 핵심 로직이 비어 있으면 핵심 로직 누락으로 판단해 주세요.
                codingMistakeStats는 전체 누적 코딩테스트의 반복 실수 유형입니다. 문제별 나열보다 반복 패턴과 우선순위를 요약해 주세요.
                nextActions는 다음 코딩테스트 전에 바로 실행할 수 있는 행동으로 작성해 주세요.
                '시험' 대신 '코딩테스트'라는 표현을 사용해 주세요.
                """);
        payload.put("premiumActive", premiumActive);
        payload.put("analysisScope", Map.of(
                "type", "ALL_GRADED_CODING_TESTS",
                "anchorExamId", session.getExamId()));
        payload.put("codingTestAggregate", analysisDashboardMapper.findCodingExamAggregateByUser(userId)
                .orElseGet(AnalysisCodingExamAggregate::new));
        payload.put("allCodingTests", analysisDashboardMapper.findAllGradedExamSummariesByUser(userId));
        payload.put("recentCodingTests", analysisDashboardMapper.findRecentGradedExamSummariesByUser(userId, TREND_LIMIT));
        payload.put("subjectCodingSummaries", analysisDashboardMapper.findSubjectSummaries(userId));
        payload.put("levelCodingSummaries", analysisDashboardMapper.findLevelSummaries(userId));
        payload.put("codingAnswerSummaries", codingAnswerSummaries(userId));
        payload.put("codingMistakeStats", analysisDashboardMapper.findCodingMistakeStatsByUser(userId));
        return new ChatGptRequest(
                "exam-analysis",
                "analysis-v3",
                payload);
    }

    private List<AnalysisCodingAnswerSummary> codingAnswerSummaries(Long userId) {
        List<AnalysisCodingAnswerSummary> summaries = analysisDashboardMapper.findCodingAnswerSummaries(userId);
        summaries.forEach(summary -> summary.setStarterCode(ExamStarterCodeResolver.defaultStarterCode()));
        return summaries;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 코딩테스트를 찾을 수 없습니다."));
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

    private Object reportLock(Long userId, Long examId) {
        int index = Math.floorMod(Objects.hash(userId, examId), reportLocks.length);
        return reportLocks[index];
    }

    private Object[] initializeReportLocks() {
        return java.util.stream.IntStream.range(0, REPORT_LOCK_STRIPES)
                .mapToObj(index -> new Object())
                .toArray(Object[]::new);
    }

    private AnalysisReportResponse responseFor(Long userId, AiAnalysisReport report) {
        return AnalysisReportResponse.from(
                report,
                paymentAccessService.hasPremiumAccess(userId)
        );
    }

    private record ReportClaim(AiAnalysisReport report, boolean created) {}
}
