package com.acorn.elearning.payment.view;

import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.user.dto.response.PaymentHistoryPageResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record PaymentHistoryView(
        String title,
        PremiumAccessResponse premiumAccess,
        List<PaymentHistoryPageResponse.Payment> payments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static PaymentHistoryView from(PaymentHistoryPageResponse response) {
        return new PaymentHistoryView(
                "결제 내역",
                response.premiumAccess(),
                response.payments(),
                response.page(),
                response.size(),
                response.totalElements(),
                response.totalPages(),
                response.first(),
                response.last(),
                response.empty()
        );
    }

    public String premiumStatusLabel() {
        return premiumAccess != null && premiumAccess.premiumActive() ? "Premium 활성" : "Premium 비활성";
    }

    public String premiumGrantTypeLabel() {
        if (premiumAccess == null || premiumAccess.grantType() == null || premiumAccess.grantType().isBlank()) {
            return "-";
        }
        if ("LIFETIME".equals(premiumAccess.grantType())) {
            return "Lifetime";
        }
        return premiumAccess.grantType();
    }

    public String premiumGrantedAtLabel() {
        if (premiumAccess == null) {
            return "-";
        }
        return formatDateTime(premiumAccess.grantedAt());
    }

    public String premiumExpiresAtLabel() {
        if (premiumAccess == null || !premiumAccess.premiumActive()) {
            return "-";
        }
        if (premiumAccess.expiresAt() == null) {
            return "무제한";
        }
        return formatDateTime(premiumAccess.expiresAt());
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }
}
