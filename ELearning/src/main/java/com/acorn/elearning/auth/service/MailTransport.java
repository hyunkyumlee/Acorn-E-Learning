package com.acorn.elearning.auth.service;

public interface MailTransport {

    void sendResetLink(String toEmail, String resetUrl, long ttlMinutes);
}
