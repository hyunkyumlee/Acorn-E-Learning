package com.acorn.knowva.mail;

@FunctionalInterface
public interface MailSender {

    String send(String toEmail, String resetUrl, long ttlMinutes);
}
