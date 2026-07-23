package com.acorn.knowva.mail;

public record MailLambdaRequest(
        String schemaVersion,
        String mailType,
        String toEmail,
        String resetUrl,
        long ttlMinutes,
        String requestId) {}
