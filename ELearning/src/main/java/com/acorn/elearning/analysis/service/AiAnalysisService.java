package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisStatusResponse;
import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
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
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAnalysisService {
    private static final String TARGET_TYPE = "ANALYSIS_REPORT";

    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final ExamSessionMapper examSessionMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final PaymentAccessService paymentAccessService;
    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(
            AiAnalysisReportMapper aiAnalysisReportMapper,
            ExamSessionMapper examSessionMapper,
            ExamAnswerMapper examAnswerMapper,
            PaymentAccessService paymentAccessService,
            ChatGptApiClient chatGptApiClient,
            AiRequestLogService aiRequestLogService,
            ObjectMapper objectMapper
    ) {
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
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

    @Transactional
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

    @Transactional
    public AnalysisReportResponse retry(SessionUser sessionUser, Long reportId) {
        Long userId = requireUserId(sessionUser);
        AiAnalysisReport report = requireReport(userId, reportId);
        ExamSession session = requireExam(userId, report.getExamId());
        report.setStatus("PENDING");
        report.setRetryCount(report.getRetryCount() + 1);
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
        return new ChatGptRequest(
                "exam-analysis",
                "analysis-v1",
                Map.of(
                        "instruction", "한국어 존댓말로 학습 분석을 JSON으로 작성하세요. 필드는 freeSummary, premiumDetail을 사용하세요.",
                        "premiumActive", premiumActive,
                        "examSession", session,
                        "answers", examAnswerMapper.findByExamId(session.getExamId())));
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
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험을 찾을 수 없습니다."));
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
}
