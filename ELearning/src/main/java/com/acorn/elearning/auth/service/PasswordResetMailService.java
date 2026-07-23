package com.acorn.elearning.auth.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordResetMailService {

    private final MailTransport mailTransport;

    public PasswordResetMailService(MailTransport mailTransport) {
        this.mailTransport = mailTransport;
    }

    public void sendResetLink (String toEmail, String resetUrl, long ttlMinutes) {
        mailTransport.sendResetLink(toEmail, resetUrl, ttlMinutes);
    }
}
