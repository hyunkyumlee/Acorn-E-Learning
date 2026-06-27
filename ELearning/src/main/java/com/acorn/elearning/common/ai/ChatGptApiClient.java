package com.acorn.elearning.common.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatGptApiClient {
    private final String provider;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public ChatGptApiClient(
            @Value("${knowva.ai.provider:openai}") String provider,
            @Value("${knowva.ai.api-key:}") String apiKey,
            @Value("${knowva.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${knowva.ai.model:}") String model) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public ChatGptResponse send(ChatGptRequest request) {
        return ChatGptResponse.skeleton(provider, baseUrl, model, request.purpose(), apiKeyConfigured());
    }

    private boolean apiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
