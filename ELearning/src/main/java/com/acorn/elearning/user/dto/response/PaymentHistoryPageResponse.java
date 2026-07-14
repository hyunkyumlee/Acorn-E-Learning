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
            String pgProvider,
            String pgTransactionId,
            BigDecimal amount,
            LocalDateTime paidAt,
            LocalDateTime paymentCreatedAt,
            Long grantId,
            String grantType,
            String grantStatus,
            boolean premiumActive,
            LocalDateTime grantedAt,
            LocalDateTime expiresAt,
            Long refundId,
            String refundStatus,
            BigDecimal refundAmount,
            String refundReason,
            String refundPgProvider,
            String pgRefundTransactionId,
            LocalDateTime refundRequestedAt,
            LocalDateTime refundCompletedAt,
            LocalDateTime refundFailedAt,
            String refundFailureCode
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
                    item.getPgProvider(),
                    item.getPgTransactionId(),
                    item.getAmount(),
                    item.getPaidAt(),
                    item.getPaymentCreatedAt(),
                    item.getGrantId(),
                    item.getGrantType(),
                    item.getGrantStatus(),
                    "ACTIVE".equals(item.getGrantStatus()),
                    item.getGrantedAt(),
                    item.getExpiresAt(),
                    item.getRefundId(),
                    item.getRefundStatus(),
                    item.getRefundAmount(),
                    item.getRefundReason(),
                    item.getRefundPgProvider(),
                    item.getPgRefundTransactionId(),
                    item.getRefundRequestedAt(),
                    item.getRefundCompletedAt(),
                    item.getRefundFailedAt(),
                    item.getRefundFailureCode()
            );
        }

        public String productNameLabel() {
            return hasText(productName) ? productName : "Premium";
        }

        public String paymentMethodLabel() {
            if ("CARD".equals(paymentMethod)) {
                return "일반 카드";
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
                return "일반 카드";
            }
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                return "무통장 입금 더미 승인";
            }
            if ("KAKAO_PAY".equals(paymentMethod)) {
                return "카카오페이";
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
            if ("CANCELED".equals(paymentStatus)) {
                return "결제 취소";
            }
            if ("REFUNDED".equals(paymentStatus)) {
                return "환불 완료";
            }
            return hasText(paymentStatus) ? paymentStatus : "-";
        }

        public String historyStatusLabel() {
            if (hasRefund()) {
                return refundStatusLabel();
            }
            return paymentStatusLabel();
        }

        public String historyStatusTone() {
            if ("COMPLETED".equals(refundStatus) || "REFUNDED".equals(paymentStatus)) {
                return "refunded";
            }
            if ("FAILED".equals(refundStatus)
                    || "FAILED".equals(paymentStatus)
                    || "CANCELED".equals(paymentStatus)) {
                return "danger";
            }
            if ("PENDING".equals(refundStatus) || "READY".equals(paymentStatus)) {
                return "pending";
            }
            return "success";
        }

        public String pgProviderLabel() {
            return pgProviderLabel(pgProvider);
        }

        public String pgTransactionIdLabel() {
            return hasText(pgTransactionId) ? pgTransactionId : "-";
        }

        public String premiumStatusLabel() {
            if ("ACTIVE".equals(grantStatus)) {
                return "Premium 활성";
            }
            if ("REVOKED".equals(grantStatus)) {
                return "Premium 해제";
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
            return formatDateTime(paidAt != null ? paidAt : paymentCreatedAt);
        }

        public String paymentCreatedAtLabel() {
            return formatDateTime(paymentCreatedAt);
        }

        public String grantedAtLabel() {
            return formatDateTime(grantedAt);
        }

        public String expiresAtLabel() {
            if ("REVOKED".equals(grantStatus)) {
                return "권한 해제";
            }
            if (expiresAt == null && grantId != null) {
                return "무제한";
            }
            return formatDateTime(expiresAt);
        }

        public boolean hasRefund() {
            return refundId != null;
        }

        public String refundStatusLabel() {
            if ("PENDING".equals(refundStatus)) {
                return "환불 처리 중";
            }
            if ("COMPLETED".equals(refundStatus)) {
                return "환불 완료";
            }
            if ("FAILED".equals(refundStatus)) {
                return "환불 실패";
            }
            return hasText(refundStatus) ? refundStatus : "-";
        }

        public String refundAmountLabel() {
            if (refundAmount == null) {
                return "-";
            }
            return NumberFormat.getNumberInstance(Locale.KOREA).format(refundAmount) + "원";
        }

        public String refundReasonLabel() {
            return hasText(refundReason) ? refundReason : "-";
        }

        public String refundPgProviderLabel() {
            return pgProviderLabel(refundPgProvider);
        }

        public String pgRefundTransactionIdLabel() {
            return hasText(pgRefundTransactionId) ? pgRefundTransactionId : "-";
        }

        public String refundRequestedAtLabel() {
            return formatDateTime(refundRequestedAt);
        }

        public String refundCompletedAtLabel() {
            return formatDateTime(refundCompletedAt);
        }

        public String refundFailedAtLabel() {
            return formatDateTime(refundFailedAt);
        }

        public String refundFailureCodeLabel() {
            return hasText(refundFailureCode) ? refundFailureCode : "-";
        }

        private String pgProviderLabel(String value) {
            if ("TOSS_PAYMENTS".equals(value)) {
                return "토스페이먼츠";
            }
            if ("KAKAO_PAY".equals(value)) {
                return "카카오페이";
            }
            return hasText(value) ? value : "-";
        }

        private String formatDateTime(LocalDateTime value) {
            return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
