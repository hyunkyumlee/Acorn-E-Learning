package com.acorn.elearning.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PasswordResetMailServiceTest {

    @Test
    void delegates_reset_link_to_selected_transport() {
        CapturingTransport transport = new CapturingTransport();
        PasswordResetMailService service = new PasswordResetMailService(transport);

        service.sendResetLink("user@example.com", "https://knowva.example/reset", 30);

        assertEquals("user@example.com", transport.toEmail);
        assertEquals("https://knowva.example/reset", transport.resetUrl);
        assertEquals(30, transport.ttlMinutes);
    }

    private static final class CapturingTransport implements MailTransport {
        private String toEmail;
        private String resetUrl;
        private long ttlMinutes;

        @Override
        public void sendResetLink(String toEmail, String resetUrl, long ttlMinutes) {
            this.toEmail = toEmail;
            this.resetUrl = resetUrl;
            this.ttlMinutes = ttlMinutes;
        }
    }
}
