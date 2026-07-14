package com.acorn.elearning.payment.service;

import com.acorn.elearning.analysis.mapper.AiAnalysisReportMapper;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.mapper.DummyPaymentMapper;
import com.acorn.elearning.payment.mapper.PaymentRefundMapper;
import com.acorn.elearning.payment.mapper.PremiumGrantMapper;
import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PaymentRefund;
import com.acorn.elearning.payment.model.PremiumGrant;
import com.acorn.elearning.security.SessionUser;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PaymentRefundService {
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String REFUND_STATUS_PENDING = "PENDING";
    private static final String REFUND_STATUS_COMPLETED = "COMPLETED";
    private static final String GRANT_STATUS_ACTIVE = "ACTIVE";
    private static final String PG_TOSS_PAYMENTS = "TOSS_PAYMENTS";
    private static final String PG_KAKAO_PAY = "KAKAO_PAY";
    private static final String DEFAULT_REFUND_REASON = "사용자 환불 요청";

    private final DummyPaymentMapper dummyPaymentMapper;
    private final PaymentRefundMapper paymentRefundMapper;
    private final PremiumGrantMapper premiumGrantMapper;
    private final AiAnalysisReportMapper aiAnalysisReportMapper;
    private final TossPaymentService tossPaymentService;
    private final KakaoPayService kakaoPayService;
    private final TransactionTemplate transactionTemplate;

    public PaymentRefundService(
            DummyPaymentMapper dummyPaymentMapper,
            PaymentRefundMapper paymentRefundMapper,
            PremiumGrantMapper premiumGrantMapper,
            AiAnalysisReportMapper aiAnalysisReportMapper,
            TossPaymentService tossPaymentService,
            KakaoPayService kakaoPayService,
            PlatformTransactionManager transactionManager
    ) {
        this.dummyPaymentMapper = dummyPaymentMapper;
        this.paymentRefundMapper = paymentRefundMapper;
        this.premiumGrantMapper = premiumGrantMapper;
        this.aiAnalysisReportMapper = aiAnalysisReportMapper;
        this.tossPaymentService = tossPaymentService;
        this.kakaoPayService = kakaoPayService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public PaymentRefund refund(SessionUser sessionUser, Long paymentId) {
        return refund(requireUserId(sessionUser), paymentId, DEFAULT_REFUND_REASON);
    }

    public PaymentRefund refund(Long userId, Long paymentId, String refundReason) {
        PendingRefund pendingRefund = inTransaction(() -> requestRefund(userId, paymentId, refundReason));

        String pgRefundTransactionId;
        try {
            pgRefundTransactionId = cancelAtPg(pendingRefund);
        } catch (BusinessException exception) {
            markFailed(pendingRefund.refund().getPaymentId(), failureCode(pendingRefund.refund()));
            throw exception;
        } catch (RuntimeException exception) {
            markFailed(pendingRefund.refund().getPaymentId(), failureCode(pendingRefund.refund()));
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "환불 처리 중 알 수 없는 오류가 발생했습니다.");
        }

        return inTransaction(() -> completeRefund(pendingRefund, pgRefundTransactionId));
    }

    private PendingRefund requestRefund(Long userId, Long paymentId, String refundReason) {
        requireUserId(userId);
        if (paymentId == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 ID가 필요합니다.");
        }

        DummyPayment payment = dummyPaymentMapper.findByIdAndUserIdForUpdate(paymentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        validateRefundablePayment(payment);

        if (aiAnalysisReportMapper.existsSuccessfulReportCreatedAfter(userId, payment.getPaidAt())) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "분석 기능을 사용한 결제는 취소할 수 없습니다."
            );
        }

        PaymentRefund existingRefund = paymentRefundMapper.findByPaymentIdForUpdate(paymentId).orElse(null);
        if (existingRefund != null) {
            throw refundAlreadyRequested(existingRefund);
        }

        PremiumGrant grant = premiumGrantMapper.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "Premium 권한 정보를 찾을 수 없습니다."));
        if (!GRANT_STATUS_ACTIVE.equals(grant.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 비활성화된 Premium 권한입니다.");
        }

        PaymentRefund refund = new PaymentRefund();
        refund.setPaymentId(payment.getPaymentId());
        refund.setUserId(userId);
        refund.setRefundAmount(payment.getAmount());
        refund.setRefundReason(normalizeReason(refundReason));
        refund.setPgProvider(payment.getPgProvider());
        paymentRefundMapper.insertPending(refund);

        return new PendingRefund(payment, refund);
    }

    private PaymentRefund completeRefund(PendingRefund pendingRefund, String pgRefundTransactionId) {
        Long paymentId = pendingRefund.payment().getPaymentId();
        Long userId = pendingRefund.payment().getUserId();
        PaymentRefund refund = paymentRefundMapper.findByPaymentIdForUpdate(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "환불 정보를 찾을 수 없습니다."));
        if (!REFUND_STATUS_PENDING.equals(refund.getRefundStatus())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "처리할 수 없는 환불 상태입니다.");
        }

        DummyPayment payment = dummyPaymentMapper.findByIdAndUserIdForUpdate(paymentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        if (!PAYMENT_STATUS_PAID.equals(payment.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "결제 상태가 이미 변경되었습니다.");
        }

        if (paymentRefundMapper.markCompleted(refund.getRefundId(), pgRefundTransactionId) != 1) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "환불 완료 상태를 반영하지 못했습니다.");
        }
        if (dummyPaymentMapper.markRefunded(paymentId, userId) != 1) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "결제 환불 상태를 반영하지 못했습니다.");
        }
        if (premiumGrantMapper.revokeByPaymentIdAndUserId(paymentId, userId, refund.getRefundReason()) != 1) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "Premium 권한을 회수하지 못했습니다.");
        }

        return paymentRefundMapper.findByPaymentId(paymentId)
                .filter(item -> REFUND_STATUS_COMPLETED.equals(item.getRefundStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "환불 완료 정보를 확인하지 못했습니다."));
    }

    private String cancelAtPg(PendingRefund pendingRefund) {
        DummyPayment payment = pendingRefund.payment();
        PaymentRefund refund = pendingRefund.refund();
        return switch (payment.getPgProvider()) {
            case PG_TOSS_PAYMENTS -> tossPaymentService.cancel(
                    payment.getPgTransactionId(),
                    refund.getRefundAmount(),
                    refund.getRefundReason(),
                    "refund-" + refund.getRefundId()
            );
            case PG_KAKAO_PAY -> kakaoPayService.cancel(payment.getPgTransactionId(), refund.getRefundAmount());
            default -> throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "환불을 지원하지 않는 PG 결제입니다.");
        };
    }

    private void markFailed(Long paymentId, String failureCode) {
        inTransaction(() -> {
            paymentRefundMapper.findByPaymentIdForUpdate(paymentId)
                    .filter(refund -> REFUND_STATUS_PENDING.equals(refund.getRefundStatus()))
                    .ifPresent(refund -> paymentRefundMapper.markFailed(refund.getRefundId(), failureCode));
            return Boolean.TRUE;
        });
    }

    private void validateRefundablePayment(DummyPayment payment) {
        if (!PAYMENT_STATUS_PAID.equals(payment.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "결제 완료된 내역만 취소할 수 있습니다.");
        }
        if (payment.getPaidAt() == null) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "결제 완료 시각을 확인할 수 없습니다.");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "환불 금액을 확인할 수 없습니다.");
        }
        if (!hasText(payment.getPgProvider()) || !hasText(payment.getPgTransactionId())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "PG 거래 식별 정보가 없어 환불할 수 없습니다.");
        }
        if (!PG_TOSS_PAYMENTS.equals(payment.getPgProvider()) && !PG_KAKAO_PAY.equals(payment.getPgProvider())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "환불을 지원하지 않는 PG 결제입니다.");
        }
    }

    private BusinessException refundAlreadyRequested(PaymentRefund refund) {
        if (REFUND_STATUS_COMPLETED.equals(refund.getRefundStatus())) {
            return new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 환불 완료된 결제입니다.");
        }
        if (REFUND_STATUS_PENDING.equals(refund.getRefundStatus())) {
            return new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "환불 요청이 이미 처리 중입니다.");
        }
        return new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "환불 요청 이력이 있는 결제입니다.");
    }

    private String failureCode(PaymentRefund refund) {
        return refund.getPgProvider() + "_CANCEL_FAILED";
    }

    private String normalizeReason(String refundReason) {
        String normalized = hasText(refundReason) ? refundReason.trim() : DEFAULT_REFUND_REASON;
        if (normalized.length() > 200) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "환불 사유는 200자 이하여야 합니다.");
        }
        return normalized;
    }

    private <T> T inTransaction(TransactionCallback<T> callback) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> callback.execute()));
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record PendingRefund(DummyPayment payment, PaymentRefund refund) {}

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }
}
