package com.acorn.elearning.analysis.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acorn.elearning.analysis.mapper.AnalysisDashboardMapper;
import com.acorn.elearning.analysis.model.AiAnalysisReport;
import com.acorn.elearning.analysis.model.AnalysisCodingAnswerSummary;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.exam.mapper.ExamSessionMapper;
import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.service.AiRequestLogService;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.ObjectMapper;

final class AiAnalysisServiceTestFixtures {
    static final Long USER_ID = 3L;
    static final Long EXAM_ID = 7L;
    static final SessionUser USER = new SessionUser(
            USER_ID,
            "learner@example.com",
            "학습자",
            SessionUser.ROLE_USER,
            true);

    private AiAnalysisServiceTestFixtures() {
    }

    static AiAnalysisService service(
            InMemoryAiAnalysisReportMapper reportMapper,
            CountingChatGptApiClient client
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        AnalysisDashboardMapper dashboardMapper = mock(AnalysisDashboardMapper.class);
        ExamSessionMapper examSessionMapper = mock(ExamSessionMapper.class);
        PaymentAccessService paymentAccessService = mock(PaymentAccessService.class);

        when(dashboardMapper.findLatestGradedExamSummary(USER_ID)).thenReturn(Optional.of(examSummary()));
        when(dashboardMapper.findCodingExamAggregateByUser(anyLong())).thenReturn(Optional.of(new AnalysisCodingExamAggregate()));
        when(dashboardMapper.findAllGradedExamSummariesByUser(anyLong())).thenReturn(cumulativeExamSummaries());
        when(dashboardMapper.findRecentGradedExamSummariesByUser(anyLong(), anyInt())).thenReturn(cumulativeExamSummaries());
        when(dashboardMapper.findSubjectSummaries(anyLong())).thenReturn(List.of());
        when(dashboardMapper.findLevelSummaries(anyLong())).thenReturn(List.of());
        when(dashboardMapper.findCodingAnswerSummaries(anyLong())).thenReturn(cumulativeAnswerSummaries());
        when(dashboardMapper.findCodingMistakeStatsByUser(anyLong())).thenReturn(List.of());
        when(examSessionMapper.findByIdAndUserId(EXAM_ID, USER_ID)).thenReturn(Optional.of(examSession()));
        when(paymentAccessService.hasPremiumAccess(USER_ID)).thenReturn(true);

        return new AiAnalysisService(
                reportMapper,
                dashboardMapper,
                examSessionMapper,
                paymentAccessService,
                client,
                new AiRequestLogService(new NoopAiRequestLogMapper(), objectMapper),
                objectMapper);
    }

    static AiAnalysisReport successReport(Long userId, Long examId) {
        AiAnalysisReport report = report(userId, examId, "SUCCESS");
        report.setFreeSummary("누적 코딩테스트 흐름이 안정적입니다.");
        report.setPremiumDetail("{\"strengths\":[],\"weaknesses\":[],\"nextActions\":[]}");
        return report;
    }

    static AiAnalysisReport report(Long userId, Long examId, String status) {
        AiAnalysisReport report = new AiAnalysisReport();
        report.setUserId(userId);
        report.setExamId(examId);
        report.setStatus(status);
        report.setRetryCount(0);
        return report;
    }

    private static AnalysisExamSummary examSummary() {
        return examSummary(EXAM_ID, 2, "FAILED");
    }

    private static List<AnalysisExamSummary> cumulativeExamSummaries() {
        return List.of(
                examSummary(EXAM_ID, 2, "FAILED"),
                examSummary(6L, 1, "FAILED"));
    }

    private static AnalysisExamSummary examSummary(Long examId, int correctCount, String resultStatus) {
        AnalysisExamSummary summary = new AnalysisExamSummary();
        summary.setExamId(examId);
        summary.setUserId(USER_ID);
        summary.setSubjectId(1L);
        summary.setSubjectCode("JAVA");
        summary.setSubjectName("Java");
        summary.setLevelCode("BRONZE");
        summary.setStatus("GRADED");
        summary.setResultStatus(resultStatus);
        summary.setTotalProblemCount(3);
        summary.setCorrectCount(correctCount);
        summary.setRetryCount(0);
        summary.setGradedAt(LocalDateTime.now());
        return summary;
    }

    private static List<AnalysisCodingAnswerSummary> cumulativeAnswerSummaries() {
        AnalysisCodingAnswerSummary first = new AnalysisCodingAnswerSummary();
        first.setExamId(EXAM_ID);
        first.setSubjectName("Java");
        first.setLevelCode("BRONZE");
        first.setProblemNo(1);
        first.setPrompt("정수 score가 70 이상이면 pass를 출력하세요.");
        first.setStarterCode(starterCode());
        first.setSubmittedCode(starterCode() + "\nif (score >= 70) { System.out.println(\"pass\"); }");
        first.setPassedCaseCount(3);
        first.setCorrect(true);
        first.setAiReview("정답입니다.");

        AnalysisCodingAnswerSummary second = new AnalysisCodingAnswerSummary();
        second.setExamId(6L);
        second.setSubjectName("Java");
        second.setLevelCode("BRONZE");
        second.setProblemNo(2);
        second.setPrompt("1부터 n까지의 합을 출력하세요.");
        second.setStarterCode(starterCode());
        second.setSubmittedCode(starterCode());
        second.setPassedCaseCount(0);
        second.setCorrect(false);
        second.setAiReview("핵심 구현이 비어 있습니다.");

        return List.of(first, second);
    }

    private static String starterCode() {
        return "import java.util.Scanner; public class Solution { public static void main(String[] args) { Scanner scanner = new Scanner(System.in); } }";
    }

    private static ExamSession examSession() {
        ExamSession session = new ExamSession();
        session.setExamId(EXAM_ID);
        session.setUserId(USER_ID);
        session.setSubjectId(1L);
        session.setLevelCode("BRONZE");
        session.setStatus("GRADED");
        session.setTotalProblemCount(3);
        session.setCorrectCount(2);
        return session;
    }
}
