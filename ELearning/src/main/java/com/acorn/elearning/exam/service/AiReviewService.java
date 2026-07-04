package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.ai.ChatGptApiClient;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiRequestLog;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiReviewService {
    private static final String TARGET_TYPE = "EXAM_ANSWER";
    private static final String REVIEW_INSTRUCTION = """
            한국어 존댓말로 해설과 코드 리뷰를 JSON으로 작성하세요.
            필드는 explanation, codeReview만 사용하세요.
            starterCode는 시스템이 제공한 기본 구조이며 사용자가 작성한 코드로 평가하지 마세요.
            import, Scanner 입력, class/main 구조, TODO 주석처럼 starterCode에 이미 있던 부분은 칭찬하거나 분석하지 마세요.
            submittedCode에서 starterCode 대비 실제로 구현된 로직과 출력 결과만 리뷰하세요.
            구현이 비어 있거나 TODO가 남아 있으면 핵심 로직이 빠졌다고 직접 안내하세요.
            """;

    private final ChatGptApiClient chatGptApiClient;
    private final AiRequestLogService aiRequestLogService;

    public AiReviewService(ChatGptApiClient chatGptApiClient, AiRequestLogService aiRequestLogService) {
        this.chatGptApiClient = chatGptApiClient;
        this.aiRequestLogService = aiRequestLogService;
    }

    public String reviewAnswer(
            Long answerId,
            String prompt,
            String starterCode,
            String submittedCode,
            TestCaseExecutionResult executionResult
    ) {
        ChatGptRequest request = new ChatGptRequest(
                "exam-answer-review",
                "exam-review-v2",
                Map.of(
                        "instruction", REVIEW_INSTRUCTION,
                        "problem", prompt,
                        "starterCode", starterCode,
                        "submittedCode", submittedCode,
                        "reviewRules", List.of(
                                "starterCode는 시스템 제공 코드입니다.",
                                "기본 구조, 입력 처리, import는 starterCode에 있으면 사용자 작성물로 평가하지 않습니다.",
                                "코드 리뷰는 submittedCode의 구현 로직과 테스트 실패 원인 중심으로 작성합니다."),
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
