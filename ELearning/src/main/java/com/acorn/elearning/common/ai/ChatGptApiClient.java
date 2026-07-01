package com.acorn.elearning.common.ai;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatGptApiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String provider;
    private final boolean enabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int maxCompletionTokens;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ChatGptApiClient(
            @Value("${knowva.ai.provider:openai}") String provider,
            @Value("${knowva.ai.enabled:false}") boolean enabled,
            @Value("${knowva.ai.api-key:}") String apiKey,
            @Value("${knowva.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${knowva.ai.model:}") String model,
            @Value("${knowva.ai.max-completion-tokens:800}") int maxCompletionTokens,
            ObjectMapper objectMapper
    ) {
        this.provider = provider;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.maxCompletionTokens = maxCompletionTokens;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    public ChatGptResponse send(ChatGptRequest request) {
        requireConfiguration();
        try {
            String requestBody = objectMapper.writeValueAsString(body(request));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedBaseUrl() + "/chat/completions"))
                    .timeout(TIMEOUT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "ChatGPT API 호출에 실패했습니다.");
            }

            String content = extractContent(response.body());
            return new ChatGptResponse(
                    "SUCCESS",
                    provider,
                    baseUrl,
                    model,
                    request.purpose(),
                    content,
                    response.body(),
                    Map.of("promptVersion", request.promptVersion()));
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "ChatGPT 요청을 만들 수 없습니다.");
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "ChatGPT API 응답을 받을 수 없습니다.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "ChatGPT API 호출이 중단되었습니다.");
        }
    }

    private Map<String, Object> body(ChatGptRequest request) {
        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are Knowva's coding education assistant. Return valid JSON only."),
                        Map.of(
                                "role", "user",
                                "content", objectMapper.valueToTree(request.payload()).toString())),
                "temperature", 0.2,
                "max_completion_tokens", maxCompletionTokens,
                "response_format", Map.of("type", "json_object"));
    }

    private String extractContent(String responseBody) throws JacksonException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "ChatGPT API 응답 본문이 비어 있습니다.");
        }
        return content.asText();
    }

    private void requireConfiguration() {
        if (!enabled) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "AI 기능이 비활성화되어 있습니다.");
        }
        if (!"openai".equalsIgnoreCase(provider)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "지원하지 않는 AI provider입니다.");
        }
        if (apiKey == null || apiKey.isBlank() || model == null || model.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "ChatGPT API 설정이 필요합니다.");
        }
    }

    private String normalizedBaseUrl() {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
