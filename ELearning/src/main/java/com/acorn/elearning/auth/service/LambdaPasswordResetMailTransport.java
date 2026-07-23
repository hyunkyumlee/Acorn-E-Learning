package com.acorn.elearning.auth.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;

public class LambdaPasswordResetMailTransport implements MailTransport {

    private final LambdaInvoker lambdaInvoker;
    private final ObjectMapper objectMapper;
    private final String functionName;

    public LambdaPasswordResetMailTransport(
            LambdaInvoker lambdaInvoker, ObjectMapper objectMapper, String functionName) {
        this.lambdaInvoker = lambdaInvoker;
        this.objectMapper = objectMapper;
        this.functionName = requireFunctionName(functionName);
    }

    @Override
    public void sendResetLink(String toEmail, String resetUrl, long ttlMinutes) {
        String requestId = UUID.randomUUID().toString();
        LambdaMailRequest request = new LambdaMailRequest(
                "1", "PASSWORD_RESET", toEmail, resetUrl, ttlMinutes, requestId);

        try {
            String payload = objectMapper.writeValueAsString(request);
            LambdaInvokeResult result = lambdaInvoker.invoke(functionName, payload);
            validateInvocation(result);
            LambdaMailResult mailResult = objectMapper.readValue(result.payload(), LambdaMailResult.class);
            if (!mailResult.success()) {
                throw mailFailure();
            }
        } catch (RuntimeException exception) {
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw mailFailure();
        }
    }

    private void validateInvocation(LambdaInvokeResult result) {
        if (result == null
                || result.statusCode() < 200
                || result.statusCode() >= 300
                || result.functionError() != null
                || result.payload() == null
                || result.payload().isBlank()) {
            throw mailFailure();
        }
    }

    private BusinessException mailFailure() {
        return new BusinessException(ErrorCode.AUTH_MAIL_SEND_FAILED);
    }

    private String requireFunctionName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("MAIL_LAMBDA_FUNCTION_NAME must be configured when Lambda mail transport is enabled");
        }
        return value;
    }

    public record LambdaMailRequest(
            String schemaVersion,
            String mailType,
            String toEmail,
            String resetUrl,
            long ttlMinutes,
            String requestId) {}

    public record LambdaMailResult(boolean success, String messageId, String errorCode, String requestId) {}
}
