package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.model.PaymentHistoryItem;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public record PaymentHistoryPageResponse(
        PremiumAccessResponse premiumAccess,
        List<Payment> payments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    public static PaymentHistoryPageResponse of(
            PremiumAccessResponse premiumAccess,
            List<PaymentHistoryItem> historyItems,
            int page,
            int size,
            long totalElements
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        long safeTotalElements = Math.max(totalElements, 0);
        int totalPages = safeTotalElements == 0
                ? 0
                : (int) Math.ceil((double) safeTotalElements / safeSize);
        List<Payment> payments = historyItems == null
                ? List.of()
                : historyItems.stream()
                        .map(Payment::from)
                        .toList();

        return new PaymentHistoryPageResponse(
                premiumAccess == null ? PremiumAccessResponse.inactive() : premiumAccess,
                payments,
                safePage,
                safeSize,
                safeTotalElements,
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1,
                payments.isEmpty()
        );
    }

    public record Payment(
            Long paymentId,
            String orderNo,
            Long productId,
            String productCode,
            String productName,
            String paymentMethod,
            String paymentStatus,
            BigDecimal amount,
            LocalDateTime paidAt,
            LocalDateTime paymentCreatedAt,
            Long grantId,
            String grantType,
            String grantStatus,
            boolean premiumActive,
            LocalDateTime grantedAt,
            LocalDateTime expiresAt
    ) {
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        public static Payment from(PaymentHistoryItem item) {
            return new Payment(
                    item.getPaymentId(),
                    item.getOrderNo(),
                    item.getProductId(),
                    item.getProductCode(),
                    item.getProductName(),
                    item.getPaymentMethod(),
                    item.getPaymentStatus(),
                    item.getAmount(),
                    item.getPaidAt(),
                    item.getPaymentCreatedAt(),
                    item.getGrantId(),
                    item.getGrantType(),
                    item.getGrantStatus(),
                    "ACTIVE".equals(item.getGrantStatus()),
                    item.getGrantedAt(),
                    item.getExpiresAt()
            );
        }

        public String productNameLabel() {
            return hasText(productName) ? productName : "Premium";
        }

        public String paymentMethodLabel() {
            if ("CARD".equals(paymentMethod)) {
                return "신용카드";
            }
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                return "무통장 입금";
            }
            if ("KAKAO_PAY".equals(paymentMethod)) {
                return "카카오페이";
            }
            return hasText(paymentMethod) ? paymentMethod : "-";
        }

        public String paymentMethodDetailLabel() {
            if ("CARD".equals(paymentMethod)) {
                return "신용카드 더미 승인";
            }
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                return "무통장 입금 더미 승인";
            }
            if ("KAKAO_PAY".equals(paymentMethod)) {
                return "카카오페이 승인";
            }
            return paymentMethodLabel();
        }

        public String paymentStatusLabel() {
            if ("PAID".equals(paymentStatus)) {
                return "결제 완료";
            }
            if ("READY".equals(paymentStatus)) {
                return "결제 대기";
            }
            if ("FAILED".equals(paymentStatus)) {
                return "결제 실패";
            }
            return hasText(paymentStatus) ? paymentStatus : "-";
        }

        public String premiumStatusLabel() {
            if ("ACTIVE".equals(grantStatus)) {
                return "Premium 활성";
            }
            if (hasText(grantStatus)) {
                return grantStatus;
            }
            return "-";
        }

        public String grantTypeLabel() {
            if ("LIFETIME".equals(grantType)) {
                return "Lifetime";
            }
            return hasText(grantType) ? grantType : "-";
        }

        public String amountLabel() {
            if (amount == null) {
                return "0원";
            }
            return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
        }

        public String paidAtLabel() {
            return formatDateTime(paidAt);
        }

        public String paymentCreatedAtLabel() {
            return formatDateTime(paymentCreatedAt);
        }

        public String grantedAtLabel() {
            return formatDateTime(grantedAt);
        }

        public String expiresAtLabel() {
            if (expiresAt == null && grantId != null) {
                return "무제한";
            }
            return formatDateTime(expiresAt);
        }

        private String formatDateTime(LocalDateTime value) {
            return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
