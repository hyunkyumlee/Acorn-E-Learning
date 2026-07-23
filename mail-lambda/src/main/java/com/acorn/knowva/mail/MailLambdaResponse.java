package com.acorn.knowva.mail;

public record MailLambdaResponse(
        boolean success,
        String messageId,
        String errorCode,
        String requestId) {

    public static MailLambdaResponse success(String messageId, String requestId) {
        return new MailLambdaResponse(true, messageId, null, requestId);
    }

    public static MailLambdaResponse failure(String errorCode, String requestId) {
        return new MailLambdaResponse(false, null, errorCode, requestId);
    }
}
