package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.dto.response.AnalysisDashboardResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisAiReportView;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.analysis.mapper.AnalysisDashboardMapper;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisLevelSummary;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisSubjectSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisDashboardService {
    private static final int TREND_LIMIT = 7;

    private final AnalysisDashboardMapper analysisDashboardMapper;
    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final PaymentAccessService paymentAccessService;
    private final ObjectMapper objectMapper;

    public AnalysisDashboardService(
            AnalysisDashboardMapper analysisDashboardMapper,
            AiAnalysisReportMapper aiAnalysisReportMapper,
            PaymentAccessService paymentAccessService,
            ObjectMapper objectMapper
    ) {
        this.analysisDashboardMapper = analysisDashboardMapper;
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
        this.paymentAccessService = paymentAccessService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AnalysisDashboardResponse dashboard(SessionUser sessionUser) {
        Long userId = sessionUser.userId();
        boolean premiumActive = paymentAccessService.hasPremiumAccess(userId);
        List<AnalysisSubjectSummary> subjectSummaries = analysisDashboardMapper.findSubjectSummaries(userId);
        List<AnalysisLevelSummary> levelSummaries = analysisDashboardMapper.findLevelSummaries(userId);
        AnalysisExamSummary exam = dashboardExam(userId);
        if (exam == null) {
            AnalysisReportResponse report = latestReport(userId);
            return AnalysisDashboardResponse.empty(
                    premiumActive,
                    report,
                    AnalysisAiReportView.from(report, objectMapper),
                    subjectSummaries,
                    levelSummaries);
        }
        AnalysisReportResponse report = reportForExam(userId, exam.getExamId());
        AnalysisAiReportView aiReport = AnalysisAiReportView.from(report, objectMapper);
        Long subjectId = exam.getSubjectId();
        AnalysisPracticeSummary practiceSummary = analysisDashboardMapper.findPracticeSummary(userId, subjectId)
                .orElseGet(AnalysisPracticeSummary::new);
        AnalysisWrongAnswerSummary wrongAnswerSummary = analysisDashboardMapper.findWrongAnswerSummary(userId, subjectId)
                .orElseGet(AnalysisWrongAnswerSummary::new);
        AnalysisCodingExamAggregate codingExamAggregate = analysisDashboardMapper.findCodingExamAggregateByUser(userId)
                .orElseGet(AnalysisCodingExamAggregate::new);

        return AnalysisDashboardResponse.from(
                premiumActive,
                report,
                aiReport,
                exam,
                codingExamAggregate,
                analysisDashboardMapper.findCodingMistakeStatsByUser(userId),
                analysisDashboardMapper.findRecentGradedExamSummariesByUser(userId, TREND_LIMIT),
                analysisDashboardMapper.findLearningProgressStats(userId, subjectId),
                practiceSummary,
                wrongAnswerSummary,
                analysisDashboardMapper.findWrongAnswerNodeStats(userId, subjectId),
                subjectSummaries,
                levelSummaries
        );
    }

    private AnalysisReportResponse latestReport(Long userId) {
        return aiAnalysisReportMapper.findByUserId(userId).stream()
                .findFirst()
                .map(report -> AnalysisReportResponse.from(
                        report,
                        paymentAccessService.hasPremiumAccess(userId)
                ))
                .orElse(null);
    }

    private AnalysisReportResponse reportForExam(Long userId, Long examId) {
        return aiAnalysisReportMapper.findByExamIdAndUserId(examId, userId)
                .map(report -> AnalysisReportResponse.from(
                        report,
                        paymentAccessService.hasPremiumAccess(userId)
                ))
                .orElse(null);
    }

    private AnalysisExamSummary dashboardExam(Long userId) {
        return analysisDashboardMapper.findLatestGradedExamSummary(userId).orElse(null);
    }
}
