package com.acorn.knowva.mail;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class PasswordResetMailHandler implements RequestHandler<MailLambdaRequest, MailLambdaResponse> {

    private final MailSender mailSender;

    public PasswordResetMailHandler() {
        this(new SesV2MailSender());
    }

    PasswordResetMailHandler(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public MailLambdaResponse handleRequest(MailLambdaRequest request, Context context) {
        try {
            validate(request);
            String messageId = mailSender.send(request.toEmail(), request.resetUrl(), request.ttlMinutes());
            return MailLambdaResponse.success(messageId, request.requestId());
        } catch (InvalidMailRequestException exception) {
            return MailLambdaResponse.failure("INVALID_REQUEST", requestIdOf(request));
        } catch (MailSendException exception) {
            return MailLambdaResponse.failure("SES_SEND_FAILED", requestIdOf(request));
        } catch (RuntimeException exception) {
            return MailLambdaResponse.failure("SES_SEND_FAILED", requestIdOf(request));
        }
    }

    private void validate(MailLambdaRequest request) {
        if (request == null
                || !"1".equals(request.schemaVersion())
                || !"PASSWORD_RESET".equals(request.mailType())
                || isBlank(request.toEmail())
                || !request.toEmail().contains("@")
                || isBlank(request.resetUrl())
                || !request.resetUrl().startsWith("https://")
                || request.ttlMinutes() < 1
                || request.ttlMinutes() > 120
                || isBlank(request.requestId())) {
            throw new InvalidMailRequestException();
        }
    }

    private String requestIdOf(MailLambdaRequest request) {
        return request == null ? null : request.requestId();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static final class InvalidMailRequestException extends RuntimeException {}
}
