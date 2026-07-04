package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.model.AiRequestLog;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiReviewServiceTest {

    @Test
    void reviewAnswer_sends_starter_code_separately_from_submitted_code() {
        ObjectMapper objectMapper = new ObjectMapper();
        CapturingChatGptApiClient client = new CapturingChatGptApiClient(objectMapper);
        AiReviewService service = new AiReviewService(
                client,
                new AiRequestLogService(new NoopAiRequestLogMapper(), objectMapper));

        TestCaseExecutionResult executionResult = new TestCaseExecutionResult(
                "FAILED",
                false,
                0,
                1,
                List.of(new TestCaseExecutionResult.CaseResult("80", "pass", "", false, null)));
        String starterCode = "import java.util.Scanner;\\n// TODO 여기에 문제 풀이 로직을 작성하세요.";
        String submittedCode = starterCode + "\\nint score = scanner.nextInt();";

        service.reviewAnswer(7L, "70점 이상이면 pass를 출력하세요.", starterCode, submittedCode, executionResult);

        ChatGptRequest request = client.capturedRequest;
        assertNotNull(request);
        assertEquals("exam-review-v3", request.promptVersion());
        assertEquals(starterCode, request.payload().get("starterCode"));
        assertEquals(submittedCode, request.payload().get("submittedCode"));
        assertFalse(request.payload().containsKey("answerText"));
        assertSame(executionResult, request.payload().get("testCaseExecution"));
        assertTrue(request.payload().get("instruction").toString().contains("starterCode는 시스템이 제공한 기본 구조"));
        assertTrue(request.payload().get("instruction").toString().contains("좋은 점은 테스트를 통과한 사용자 구현 로직이 있을 때만"));
        assertTrue(request.payload().get("reviewRules").toString().contains("사용자 작성물로 평가하지 않습니다"));
        assertTrue(request.payload().get("reviewRules").toString().contains("칭찬 없이 누락된 계산"));
    }

    private static class CapturingChatGptApiClient extends ChatGptApiClient {
        private ChatGptRequest capturedRequest;

        CapturingChatGptApiClient(ObjectMapper objectMapper) {
            super("openai", true, "test-key", "https://example.com", "gpt-test", 800, objectMapper);
        }

        @Override
        public ChatGptResponse send(ChatGptRequest request) {
            this.capturedRequest = request;
            return new ChatGptResponse(
                    "SUCCESS",
                    "openai",
                    "https://example.com",
                    "gpt-test",
                    request.purpose(),
                    "{\"explanation\":\"출력이 필요합니다.\",\"codeReview\":\"핵심 로직을 작성하셔야 합니다.\"}",
                    "{}",
                    Map.of());
        }
    }

    private static class NoopAiRequestLogMapper implements AiRequestLogMapper {
        @Override
        public Optional<AiRequestLog> findById(Long id) { return Optional.empty(); }

        @Override
        public List<AiRequestLog> findByTarget(String targetType, Long targetId) { return List.of(); }

        @Override
        public List<AiRequestLog> findAll() { return List.of(); }

        @Override
        public int insert(AiRequestLog model) { return 1; }

        @Override
        public int update(AiRequestLog model) { return 1; }
    }
}
