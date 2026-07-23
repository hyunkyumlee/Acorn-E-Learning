package com.acorn.knowva.mail;

import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

public class SesV2MailSender implements MailSender {

    private static final String SUBJECT = "[Knowva] 비밀번호 재설정 안내";
    private final SesV2Client sesClient;
    private final String fromAddress;

    public SesV2MailSender() {
        this(SesV2Client.create(), System.getenv("MAIL_FROM"));
    }

    SesV2MailSender(SesV2Client sesClient, String fromAddress) {
        this.sesClient = sesClient;
        this.fromAddress = fromAddress;
    }

    @Override
    public String send(String toEmail, String resetUrl, long ttlMinutes) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new MailSendException("MAIL_FROM is not configured");
        }
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .fromEmailAddress(fromAddress)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .content(EmailContent.builder()
                            .simple(Message.builder()
                                    .subject(Content.builder().data(SUBJECT).build())
                                    .body(Body.builder()
                                            .html(Content.builder().data(renderBody(resetUrl, ttlMinutes)).build())
                                            .build())
                                    .build())
                            .build())
                    .build();
            return sesClient.sendEmail(request).messageId();
        } catch (RuntimeException exception) {
            throw new MailSendException("SES send failed", exception);
        }
    }

    private String renderBody(String resetUrl, long ttlMinutes) {
        String safeUrl = escapeHtml(resetUrl);
        return """
                <div style="max-width:520px;margin:0 auto;padding:32px 24px;font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#1f2430;">
                    <h2 style="margin:0 0 16px;">Knowva 비밀번호 재설정</h2>
                    <p>아래 버튼을 눌러 새 비밀번호를 설정해 주세요.</p>
                    <p style="margin:24px 0;"><a href="%s" style="display:inline-block;padding:12px 28px;border-radius:8px;background:#5865f2;color:#ffffff;text-decoration:none;font-weight:700;">비밀번호 재설정</a></p>
                    <p style="font-size:13px;color:#6b7280;">이 링크는 <strong>%d분</strong> 동안만 유효하며, 1회 사용 후 만료됩니다.<br>버튼이 눌리지 않으면 아래 주소를 브라우저에 붙여넣어 주세요.<br><a href="%s">%s</a></p>
                    <p style="font-size:13px;color:#6b7280;">본인이 요청하지 않았다면 이 메일을 무시해 주세요. 비밀번호는 변경되지 않습니다.</p>
                </div>
                """.formatted(safeUrl, ttlMinutes, safeUrl, safeUrl);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
