package com.acorn.elearning.common.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ChatGptApiClientTest {

    @Test
    void send_rejects_request_before_network_call_when_api_configuration_is_missing() {
        ChatGptApiClient client = new ChatGptApiClient("openai", true, "", "http://localhost:9/v1", "", 800, new ObjectMapper());
        ChatGptRequest request = new ChatGptRequest("test", "test-v1", Map.of("prompt", "hello"));

        BusinessException exception = assertThrows(BusinessException.class, () -> client.send(request));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
        assertEquals("ChatGPT API 설정이 필요합니다.", exception.getMessage());
    }

    @Test
    void send_rejects_request_before_network_call_when_ai_is_disabled() {
        ChatGptApiClient client = new ChatGptApiClient("openai", false, "key", "http://localhost:9/v1", "gpt-test", 800, new ObjectMapper());
        ChatGptRequest request = new ChatGptRequest("test", "test-v1", Map.of("prompt", "hello"));

        BusinessException exception = assertThrows(BusinessException.class, () -> client.send(request));

        assertEquals(ErrorCode.COMMON_VALIDATION_FAILED, exception.errorCode());
        assertEquals("AI 기능이 비활성화되어 있습니다.", exception.getMessage());
    }
}
