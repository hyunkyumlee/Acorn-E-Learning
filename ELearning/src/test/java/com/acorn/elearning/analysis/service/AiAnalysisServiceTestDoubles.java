package com.acorn.elearning.analysis.service;

import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.analysis.model.AiAnalysisReport;
import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.model.AiRequestLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import org.springframework.dao.DuplicateKeyException;
import tools.jackson.databind.ObjectMapper;

class CountingChatGptApiClient extends ChatGptApiClient {
    private final AtomicInteger sendCount = new AtomicInteger();
    private final long delayMillis;
    private volatile ChatGptRequest lastRequest;

    CountingChatGptApiClient(ObjectMapper objectMapper, long delayMillis) {
        super("openai", true, "test-key", "https://example.com", "gpt-test", 800, objectMapper);
        this.delayMillis = delayMillis;
    }

    @Override
    public ChatGptResponse send(ChatGptRequest request) {
        lastRequest = request;
        sendCount.incrementAndGet();
        if (delayMillis > 0) {
            sleep();
        }
        return new ChatGptResponse(
                "SUCCESS",
                "openai",
                "https://example.com",
                "gpt-test",
                request.purpose(),
                "{\"freeSummary\":\"전체 누적 코딩테스트 분석이 갱신되었습니다.\",\"premiumDetail\":{\"strengths\":[\"정답률이 개선되고 있습니다.\"],\"weaknesses\":[],\"nextActions\":[]}}",
                "{}",
                Map.of());
    }

    int sendCount() {
        return sendCount.get();
    }

    ChatGptRequest lastRequest() {
        return lastRequest;
    }

    private void sleep() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}

class FailingChatGptApiClient extends ChatGptApiClient {
    private final AtomicInteger sendCount = new AtomicInteger();
    private final long delayMillis;

    FailingChatGptApiClient(ObjectMapper objectMapper, long delayMillis) {
        super("openai", true, "test-key", "https://example.com", "gpt-test", 800, objectMapper);
        this.delayMillis = delayMillis;
    }

    @Override
    public ChatGptResponse send(ChatGptRequest request) {
        sendCount.incrementAndGet();
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트용 분석 실패입니다.");
    }

    int sendCount() {
        return sendCount.get();
    }
}

class InMemoryAiAnalysisReportMapper implements AiAnalysisReportMapper {
    private final AtomicLong sequence = new AtomicLong();
    private final List<AiAnalysisReport> reports = new ArrayList<>();
    private AiAnalysisReport duplicateReportOnNextInsert;

    @Override
    public synchronized Optional<AiAnalysisReport> findById(Long id) {
        return reports.stream()
                .filter(report -> report.getReportId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Optional<AiAnalysisReport> findByIdAndUserId(Long reportId, Long userId) {
        return reports.stream()
                .filter(report -> report.getReportId().equals(reportId) && report.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public synchronized Optional<AiAnalysisReport> findByExamId(Long examId) {
        return latest(report -> report.getExamId().equals(examId));
    }

    @Override
    public synchronized Optional<AiAnalysisReport> findByExamIdAndUserId(Long examId, Long userId) {
        return latest(report -> report.getExamId().equals(examId) && report.getUserId().equals(userId));
    }

    @Override
    public synchronized Optional<AiAnalysisReport> findByExamIdAndUserIdForUpdate(Long examId, Long userId) {
        return findByExamIdAndUserId(examId, userId);
    }

    @Override
    public synchronized boolean existsSuccessfulReportCreatedAfter(Long userId, LocalDateTime paidAt) {
        return paidAt != null && reports.stream()
                .filter(report -> userId.equals(report.getUserId()))
                .filter(report -> "SUCCESS".equals(report.getStatus()))
                .map(AiAnalysisReport::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .anyMatch(createdAt -> !createdAt.isBefore(paidAt));
    }

    @Override
    public synchronized List<AiAnalysisReport> findByUserId(Long userId) {
        return reports.stream()
                .filter(report -> report.getUserId().equals(userId))
                .sorted(Comparator.comparing(AiAnalysisReport::getReportId).reversed())
                .toList();
    }

    @Override
    public synchronized List<AiAnalysisReport> findAll() {
        return List.copyOf(reports);
    }

    @Override
    public synchronized int insert(AiAnalysisReport model) {
        if (duplicateReportOnNextInsert != null) {
            AiAnalysisReport existingReport = duplicateReportOnNextInsert;
            duplicateReportOnNextInsert = null;
            existingReport.setReportId(sequence.incrementAndGet());
            reports.add(existingReport);
            throw new DuplicateKeyException("분석 리포트가 이미 생성되었습니다.");
        }
        model.setReportId(sequence.incrementAndGet());
        reports.add(model);
        return 1;
    }

    @Override
    public synchronized int claimRetry(Long reportId, Long userId, int retryCount) {
        return reports.stream()
                .filter(report -> report.getReportId().equals(reportId) && report.getUserId().equals(userId))
                .filter(report -> "FAILED".equals(report.getStatus()))
                .filter(report -> report.getRetryCount() == retryCount)
                .findFirst()
                .map(report -> {
                    report.setStatus("PENDING");
                    report.setRetryCount(report.getRetryCount() + 1);
                    report.setAnalysisErrorCode(null);
                    return 1;
                })
                .orElse(0);
    }

    @Override
    public synchronized int update(AiAnalysisReport model) {
        return 1;
    }

    synchronized int count() {
        return reports.size();
    }

    synchronized void rejectNextInsertWithExistingReport(AiAnalysisReport report) {
        duplicateReportOnNextInsert = report;
    }

    private Optional<AiAnalysisReport> latest(Predicate<AiAnalysisReport> predicate) {
        return reports.stream()
                .filter(predicate)
                .max(Comparator.comparing(AiAnalysisReport::getReportId));
    }
}

class NoopAiRequestLogMapper implements AiRequestLogMapper {
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Optional<AiRequestLog> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<AiRequestLog> findByTarget(String targetType, Long targetId) {
        return List.of();
    }

    @Override
    public List<AiRequestLog> findAll() {
        return List.of();
    }

    @Override
    public int insert(AiRequestLog model) {
        model.setAiRequestLogId(sequence.incrementAndGet());
        return 1;
    }

    @Override
    public int update(AiRequestLog model) {
        return 1;
    }
}
