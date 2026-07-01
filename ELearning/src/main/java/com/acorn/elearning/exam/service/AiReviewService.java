package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiRequestLog;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiReviewService {
    private static final String TARGET_TYPE = "EXAM_ANSWER";

    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;

    public AiReviewService(ChatGptApiClient chatGptApiClient, AiRequestLogService aiRequestLogService) {
        this.chatGptApiClient = chatGptApiClient;
        this.aiRequestLogService = aiRequestLogService;
    }

    public String reviewAnswer(Long answerId, String prompt, String answerText, TestCaseExecutionResult executionResult) {
        ChatGptRequest request = new ChatGptRequest(
                "exam-answer-review",
                "exam-review-v1",
                Map.of(
                        "instruction", "한국어 존댓말로 해설과 코드 리뷰를 JSON으로 작성하세요. 필드는 explanation, codeReview를 사용하세요.",
                        "problem", prompt,
                        "answerText", answerText,
                        "testCaseExecution", executionResult));
        AiRequestLog log = aiRequestLogService.start(TARGET_TYPE, answerId, "EXPLANATION_REVIEW", request);
        try {
            ChatGptResponse response = chatGptApiClient.send(request);
            aiRequestLogService.success(log, response);
            return response.content();
        } catch (RuntimeException exception) {
            aiRequestLogService.failed(log, exception);
            throw exception;
        }
    }
}
