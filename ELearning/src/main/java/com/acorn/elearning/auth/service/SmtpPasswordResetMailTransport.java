package com.acorn.elearning.auth.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SmtpPasswordResetMailTransport implements MailTransport {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public SmtpPasswordResetMailTransport(JavaMailSender mailSender, String fromAddress, String fromName) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void sendResetLink(String toEmail, String resetUrl, long ttlMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("[Knowva] 비밀번호 재설정 안내");
            helper.setText(buildBody(resetUrl, ttlMinutes), true);
            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException exception) {
            throw new BusinessException(ErrorCode.AUTH_MAIL_SEND_FAILED);
        }
    }

    private String buildBody(String resetUrl, long ttlMinutes) {
        return """
                <div style="max-width:520px;margin:0 auto;padding:32px 24px;font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#1f2430;">
                    <h2 style="margin:0 0 16px;">Knowva 비밀번호 재설정</h2>
                    <p>아래 버튼을 눌러 새 비밀번호를 설정해 주세요.</p>
                    <p style="margin:24px 0;">
                        <a href="%s" style="display:inline-block;padding:12px 28px;border-radius:8px;background:#5865f2;color:#ffffff;text-decoration:none;font-weight:700;">비밀번호 재설정</a>
                    </p>
                    <p style="font-size:13px;color:#6b7280;">
                        이 링크는 <strong>%d분</strong> 동안만 유효하며, 1회 사용 후 만료됩니다.<br>
                        버튼이 눌리지 않으면 아래 주소를 브라우저에 붙여넣어 주세요.<br>
                        <a href="%s">%s</a>
                    </p>
                    <p style="font-size:13px;color:#6b7280;">본인이 요청하지 않았다면 이 메일을 무시해 주세요. 비밀번호는 변경되지 않습니다.</p>
                </div>
                """.formatted(resetUrl, ttlMinutes, resetUrl, resetUrl);
    }
}
