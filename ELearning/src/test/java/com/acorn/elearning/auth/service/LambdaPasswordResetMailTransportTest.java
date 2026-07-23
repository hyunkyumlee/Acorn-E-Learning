package com.acorn.elearning.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LambdaPasswordResetMailTransportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sends_restricted_payload_and_accepts_success_response() throws Exception {
        CapturingInvoker invoker = new CapturingInvoker(
                new LambdaInvokeResult(200, null, "{\"success\":true,\"messageId\":\"m-1\",\"requestId\":\"r-1\"}"));
        LambdaPasswordResetMailTransport transport = new LambdaPasswordResetMailTransport(
                invoker, objectMapper, "knowva-mail:live");

        transport.sendResetLink("user@example.com", "https://knowva.example/reset/token", 30);

        assertEquals("knowva-mail:live", invoker.functionName);
        JsonNode request = objectMapper.readTree(invoker.payload);
        assertEquals("1", request.get("schemaVersion").asText());
        assertEquals("PASSWORD_RESET", request.get("mailType").asText());
        assertEquals("user@example.com", request.get("toEmail").asText());
        assertEquals("https://knowva.example/reset/token", request.get("resetUrl").asText());
        assertEquals(30, request.get("ttlMinutes").asLong());
        assertEquals(36, request.get("requestId").asText().length());
        assertEquals(6, request.size());
    }

    @Test
    void maps_function_error_to_existing_mail_error() {
        LambdaInvoker invoker = (functionName, payload) ->
                new LambdaInvokeResult(200, "Unhandled", "{\"success\":false,\"errorCode\":\"SES_SEND_FAILED\"}");
        LambdaPasswordResetMailTransport transport = new LambdaPasswordResetMailTransport(
                invoker, objectMapper, "knowva-mail:live");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transport.sendResetLink("user@example.com", "https://knowva.example/reset", 30));

        assertEquals(ErrorCode.AUTH_MAIL_SEND_FAILED, exception.errorCode());
    }

    private static final class CapturingInvoker implements LambdaInvoker {
        private final LambdaInvokeResult result;
        private String functionName;
        private String payload;

        private CapturingInvoker(LambdaInvokeResult result) {
            this.result = result;
        }

        @Override
        public LambdaInvokeResult invoke(String functionName, String payload) {
            this.functionName = functionName;
            this.payload = payload;
            return result;
        }
    }
}
