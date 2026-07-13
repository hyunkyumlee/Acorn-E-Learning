package com.acorn.elearning.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.EXAM_ID;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.USER;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.USER_ID;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.report;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.service;
import static com.acorn.elearning.analysis.service.AiAnalysisServiceTestFixtures.successReport;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisAutoRefreshResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.model.AiAnalysisReport;
import com.acorn.elearning.analysis.model.AnalysisCodingAnswerSummary;
import com.acorn.elearning.common.ai.ChatGptRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiAnalysisServiceIdempotencyTest {
    @Test
    void generate_returns_existing_report_without_calling_ai_when_report_already_exists() {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        reportMapper.insert(successReport(USER_ID, EXAM_ID));
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 0);
        AiAnalysisService service = service(reportMapper, client);

        AnalysisReportResponse response = service.generate(USER, new GenerateAnalysisRequest(EXAM_ID));

        assertEquals(1L, response.reportId());
        assertEquals("SUCCESS", response.status());
        assertEquals(0, client.sendCount());
        assertEquals(1, reportMapper.count());
    }

    @Test
    void generate_returns_report_claimed_by_another_instance_without_calling_ai() {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        reportMapper.rejectNextInsertWithExistingReport(successReport(USER_ID, EXAM_ID));
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 0);
        AiAnalysisService service = service(reportMapper, client);

        AnalysisReportResponse response = service.generate(USER, new GenerateAnalysisRequest(EXAM_ID));

        assertEquals("SUCCESS", response.status());
        assertEquals(0, client.sendCount());
        assertEquals(1, reportMapper.count());
    }

    @Test
    void refreshLatestIfRequired_does_not_auto_retry_failed_report() {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        AiAnalysisReport failedReport = report(USER_ID, EXAM_ID, "FAILED");
        failedReport.setAnalysisErrorCode("COMMON_INTERNAL_ERROR");
        reportMapper.insert(failedReport);
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 0);
        AiAnalysisService service = service(reportMapper, client);

        assertFalse(service.latestRefreshRequired(USER));
        AnalysisAutoRefreshResponse response = service.refreshLatestIfRequired(USER);

        assertFalse(response.attempted());
        assertEquals("FAILED", response.report().status());
        assertEquals(0, client.sendCount());
        assertEquals(1, reportMapper.count());
    }

    @Test
    void generate_sends_all_coding_test_context_to_ai() {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 0);
        AiAnalysisService service = service(reportMapper, client);

        service.generate(USER, new GenerateAnalysisRequest(EXAM_ID));

        ChatGptRequest request = client.lastRequest();
        assertNotNull(request);
        assertEquals("exam-analysis", request.purpose());
        assertEquals("analysis-v3", request.promptVersion());
        assertTrue(request.payload().get("instruction").toString().contains("모든 코딩테스트"));
        assertTrue(request.payload().get("instruction").toString().contains("strengths는 정답 처리된 제출"));
        assertTrue(request.payload().get("instruction").toString().contains("starterCode와 submittedCode를 비교"));
        assertTrue(request.payload().get("instruction").toString().contains("핵심 로직 누락"));
        assertEquals("ALL_GRADED_CODING_TESTS", ((java.util.Map<?, ?>) request.payload().get("analysisScope")).get("type"));
        assertEquals(2, ((List<?>) request.payload().get("allCodingTests")).size());
        assertEquals(2, ((List<?>) request.payload().get("recentCodingTests")).size());
        assertEquals(2, ((List<?>) request.payload().get("codingAnswerSummaries")).size());
        AnalysisCodingAnswerSummary answerSummary =
                (AnalysisCodingAnswerSummary) ((List<?>) request.payload().get("codingAnswerSummaries")).get(0);
        assertNotNull(answerSummary.getStarterCode());
        assertNotNull(answerSummary.getSubmittedCode());
        assertTrue((Boolean) request.payload().get("premiumActive"));
        assertFalse(request.payload().containsKey("latestCodingTest"));
        assertFalse(request.payload().containsKey("latestCodingTestAnswers"));
        assertFalse(request.payload().containsKey("practiceSummary"));
        assertFalse(request.payload().containsKey("learningProgress"));
    }

    @Test
    void refreshLatestIfRequired_allows_only_one_ai_call_for_concurrent_auto_refresh() throws Exception {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 150);
        AiAnalysisService service = service(reportMapper, client);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<AnalysisAutoRefreshResponse> task = () -> {
            start.await(1, TimeUnit.SECONDS);
            return service.refreshLatestIfRequired(USER);
        };

        try {
            Future<AnalysisAutoRefreshResponse> first = executor.submit(task);
            Future<AnalysisAutoRefreshResponse> second = executor.submit(task);
            start.countDown();

            List<AnalysisAutoRefreshResponse> responses = List.of(first.get(), second.get());
            long attemptedCount = responses.stream().filter(AnalysisAutoRefreshResponse::attempted).count();

            assertEquals(1, attemptedCount);
            assertEquals(1, client.sendCount());
            assertEquals(1, reportMapper.count());
            assertEquals("SUCCESS", reportMapper.findByExamIdAndUserId(EXAM_ID, USER_ID).orElseThrow().getStatus());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void retry_allows_only_one_ai_call_for_concurrent_failed_report() throws Exception {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        AiAnalysisReport failedReport = report(USER_ID, EXAM_ID, "FAILED");
        failedReport.setAnalysisErrorCode("COMMON_INTERNAL_ERROR");
        reportMapper.insert(failedReport);
        CountingChatGptApiClient client = new CountingChatGptApiClient(new ObjectMapper(), 150);
        AiAnalysisService service = service(reportMapper, client);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<AnalysisReportResponse> task = () -> {
            start.await(1, TimeUnit.SECONDS);
            return service.retry(USER, failedReport.getReportId());
        };

        try {
            Future<AnalysisReportResponse> first = executor.submit(task);
            Future<AnalysisReportResponse> second = executor.submit(task);
            start.countDown();

            List<AnalysisReportResponse> responses = List.of(first.get(), second.get());

            assertEquals(1, client.sendCount());
            assertTrue(responses.stream().allMatch(response -> "SUCCESS".equals(response.status())));
            AiAnalysisReport retriedReport = reportMapper.findById(failedReport.getReportId()).orElseThrow();
            assertEquals("SUCCESS", retriedReport.getStatus());
            assertEquals(1, retriedReport.getRetryCount());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void retry_does_not_start_a_second_ai_call_when_the_first_concurrent_retry_fails() throws Exception {
        InMemoryAiAnalysisReportMapper reportMapper = new InMemoryAiAnalysisReportMapper();
        AiAnalysisReport failedReport = report(USER_ID, EXAM_ID, "FAILED");
        failedReport.setAnalysisErrorCode("COMMON_INTERNAL_ERROR");
        reportMapper.insert(failedReport);
        FailingChatGptApiClient client = new FailingChatGptApiClient(new ObjectMapper(), 150);
        AiAnalysisService service = service(reportMapper, client);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<AnalysisReportResponse> task = () -> {
            start.await(1, TimeUnit.SECONDS);
            return service.retry(USER, failedReport.getReportId());
        };

        try {
            Future<AnalysisReportResponse> first = executor.submit(task);
            Future<AnalysisReportResponse> second = executor.submit(task);
            start.countDown();

            List<AnalysisReportResponse> responses = new ArrayList<>();
            int failureCount = 0;
            for (Future<AnalysisReportResponse> result : List.of(first, second)) {
                try {
                    responses.add(result.get());
                } catch (ExecutionException exception) {
                    failureCount++;
                }
            }

            assertEquals(1, client.sendCount());
            assertEquals(1, failureCount);
            assertEquals(1, responses.size());
            assertEquals("FAILED", responses.get(0).status());
            AiAnalysisReport retriedReport = reportMapper.findById(failedReport.getReportId()).orElseThrow();
            assertEquals("FAILED", retriedReport.getStatus());
            assertEquals(1, retriedReport.getRetryCount());
        } finally {
            executor.shutdownNow();
        }
    }

}
