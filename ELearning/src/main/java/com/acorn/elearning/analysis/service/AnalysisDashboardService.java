package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.dto.response.AnalysisDashboardResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.analysis.mapper.AnalysisDashboardMapper;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisDashboardService {
    private static final int TREND_LIMIT = 7;

    private final AnalysisDashboardMapper analysisDashboardMapper;
    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final PaymentAccessService paymentAccessService;

    public AnalysisDashboardService(
            AnalysisDashboardMapper analysisDashboardMapper,
            AiAnalysisReportMapper aiAnalysisReportMapper,
            PaymentAccessService paymentAccessService
    ) {
        this.analysisDashboardMapper = analysisDashboardMapper;
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
        this.paymentAccessService = paymentAccessService;
    }

    @Transactional(readOnly = true)
    public AnalysisDashboardResponse dashboard(SessionUser sessionUser) {
        Long userId = sessionUser.userId();
        boolean premiumActive = paymentAccessService.hasPremiumAccess(userId);
        AnalysisReportResponse report = latestReport(userId);
        AnalysisExamSummary exam = dashboardExam(userId, report);
        if (exam == null) {
            return AnalysisDashboardResponse.empty(premiumActive, report);
        }
        Long subjectId = exam.getSubjectId();
        AnalysisPracticeSummary practiceSummary = analysisDashboardMapper.findPracticeSummary(userId, subjectId)
                .orElseGet(AnalysisPracticeSummary::new);
        AnalysisWrongAnswerSummary wrongAnswerSummary = analysisDashboardMapper.findWrongAnswerSummary(userId, subjectId)
                .orElseGet(AnalysisWrongAnswerSummary::new);
        AnalysisCodingExamAggregate codingExamAggregate = analysisDashboardMapper.findCodingExamAggregate(userId, subjectId)
                .orElseGet(AnalysisCodingExamAggregate::new);

        return AnalysisDashboardResponse.from(
                premiumActive,
                report,
                exam,
                codingExamAggregate,
                analysisDashboardMapper.findCodingMistakeStats(userId, subjectId),
                analysisDashboardMapper.findRecentGradedExamSummaries(userId, subjectId, TREND_LIMIT),
                analysisDashboardMapper.findLearningProgressStats(userId, subjectId),
                practiceSummary,
                wrongAnswerSummary,
                analysisDashboardMapper.findWrongAnswerNodeStats(userId, subjectId)
        );
    }

    private AnalysisReportResponse latestReport(Long userId) {
        return aiAnalysisReportMapper.findByUserId(userId).stream()
                .findFirst()
                .map(AnalysisReportResponse::from)
                .orElse(null);
    }

    private AnalysisExamSummary dashboardExam(Long userId, AnalysisReportResponse report) {
        AnalysisExamSummary latestExam = analysisDashboardMapper.findLatestGradedExamSummary(userId).orElse(null);
        if (latestExam != null) {
            return latestExam;
        }
        if (report != null) {
            return analysisDashboardMapper.findExamSummary(userId, report.examId()).orElse(null);
        }
        return null;
    }
}
