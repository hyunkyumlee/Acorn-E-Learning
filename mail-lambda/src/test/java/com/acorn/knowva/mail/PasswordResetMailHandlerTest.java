package com.acorn.knowva.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.sesv2.model.MessageRejectedException;

class PasswordResetMailHandlerTest {

    @Test
    void sends_password_reset_mail_for_valid_request() {
        CapturingMailSender sender = new CapturingMailSender();
        PasswordResetMailHandler handler = new PasswordResetMailHandler(sender);

        MailLambdaResponse response = handler.handleRequest(
                new MailLambdaRequest("1", "PASSWORD_RESET", "user@example.com", "https://knowva.example/reset", 30, "request-1"),
                null);

        assertTrue(response.success());
        assertEquals("request-1", response.requestId());
        assertEquals("user@example.com", sender.toEmail);
        assertEquals("https://knowva.example/reset", sender.resetUrl);
        assertEquals(30, sender.ttlMinutes);
    }

    @Test
    void rejects_unsupported_mail_type_without_sending() {
        CapturingMailSender sender = new CapturingMailSender();
        PasswordResetMailHandler handler = new PasswordResetMailHandler(sender);

        MailLambdaResponse response = handler.handleRequest(
                new MailLambdaRequest("1", "ARBITRARY", "user@example.com", "https://knowva.example/reset", 30, "request-1"),
                null);

        assertFalse(response.success());
        assertEquals("INVALID_REQUEST", response.errorCode());
        assertEquals(null, sender.toEmail);
    }

    @Test
    void returns_stable_failure_when_ses_sender_fails() {
        PasswordResetMailHandler handler = new PasswordResetMailHandler((toEmail, resetUrl, ttlMinutes) -> {
            throw new MailSendException("SES rejected the message");
        });

        MailLambdaResponse response = handler.handleRequest(
                new MailLambdaRequest("1", "PASSWORD_RESET", "user@example.com", "https://knowva.example/reset", 30, "request-1"),
                null);

        assertFalse(response.success());
        assertEquals("SES_SEND_FAILED", response.errorCode());
        assertEquals("request-1", response.requestId());
    }

    @Test
    void logs_safe_ses_error_code_when_ses_sender_fails() {
        CapturingLogger logger = new CapturingLogger();
        PasswordResetMailHandler handler = new PasswordResetMailHandler((toEmail, resetUrl, ttlMinutes) -> {
            throw new MailSendException("SES rejected the message", MessageRejectedException.builder()
                    .awsErrorDetails(AwsErrorDetails.builder().errorCode("MessageRejected").build())
                    .build());
        });

        handler.handleRequest(
                new MailLambdaRequest("1", "PASSWORD_RESET", "user@example.com", "https://knowva.example/reset", 30, "request-1"),
                contextWith(logger));

        assertEquals("password_reset_mail_send_failed error_code=MessageRejected\n", logger.message);
        assertFalse(logger.message.contains("user@example.com"));
    }

    private static final class CapturingMailSender implements MailSender {
        private String toEmail;
        private String resetUrl;
        private long ttlMinutes;

        @Override
        public String send(String toEmail, String resetUrl, long ttlMinutes) {
            this.toEmail = toEmail;
            this.resetUrl = resetUrl;
            this.ttlMinutes = ttlMinutes;
            return "message-1";
        }
    }

    private static final class CapturingLogger implements LambdaLogger {
        private String message;

        @Override
        public void log(String message) {
            this.message = message;
        }

        @Override
        public void log(byte[] message) {
            this.message = new String(message);
        }
    }

    private Context contextWith(LambdaLogger logger) {
        return (Context) Proxy.newProxyInstance(
                Context.class.getClassLoader(),
                new Class<?>[]{Context.class},
                (proxy, method, arguments) -> method.getName().equals("getLogger") ? logger : null);
    }
}
