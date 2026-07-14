package com.acorn.elearning.payment.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.dto.response.PaymentDetailResponse;
import com.acorn.elearning.payment.dto.response.PaymentResultResponse;
import com.acorn.elearning.payment.form.DummyPaymentForm;
import com.acorn.elearning.payment.mapper.DummyPaymentMapper;
import com.acorn.elearning.payment.mapper.PaymentProductMapper;
import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PaymentProduct;
import com.acorn.elearning.payment.model.PremiumGrant;
import com.acorn.elearning.security.SessionUser;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DummyPaymentService {
    private static final String DEFAULT_PRODUCT_CODE = "PREMIUM_LIFETIME";
    private static final String STATUS_PAID = "PAID";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String METHOD_KAKAO_PAY = "KAKAO_PAY";

    private final DummyPaymentMapper dummyPaymentMapper;
    private final PaymentProductMapper paymentProductMapper;
    private final PremiumGrantService premiumGrantService;

    public DummyPaymentService(
            DummyPaymentMapper dummyPaymentMapper,
            PaymentProductMapper paymentProductMapper,
            PremiumGrantService premiumGrantService
    ) {
        this.dummyPaymentMapper = dummyPaymentMapper;
        this.paymentProductMapper = paymentProductMapper;
        this.premiumGrantService = premiumGrantService;
    }

    @Transactional
    public PaymentResultResponse pay(SessionUser sessionUser, DummyPaymentForm form) {
        return pay(requireUserId(sessionUser), form, null, null);
    }

    @Transactional
    public PaymentResultResponse pay(Long userId, DummyPaymentForm form) {
        return pay(userId, form, null, null);
    }

    @Transactional
    public PaymentResultResponse pay(
            Long userId,
            DummyPaymentForm form,
            String pgProvider,
            String pgTransactionId
    ) {
        requireUserId(userId);
        requireForm(form);

        PaymentProduct product = paymentProductMapper.findByCode(productCode(form))
                .filter(productItem -> Boolean.TRUE.equals(productItem.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "활성화된 결제 상품을 찾을 수 없습니다."));
        String orderNo = buildOrderNo(userId, form.getIdempotencyToken());

        Optional<DummyPayment> existingPayment = dummyPaymentMapper.findByOrderNo(orderNo);
        if (existingPayment.isPresent()) {
            return reuseExistingPayment(existingPayment.get(), userId, product, form);
        }

        if (premiumGrantService.hasActiveGrant(userId)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미 Premium 권한이 활성화되어 있습니다.");
        }

        DummyPayment payment = new DummyPayment();
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setProductId(product.getProductId());
        payment.setPaymentMethod(form.getPaymentMethod());
        payment.setPgProvider(pgProvider);
        payment.setPgTransactionId(pgTransactionId);
        payment.setAmount(product.getPrice());
        dummyPaymentMapper.insertPaid(payment);

        DummyPayment paidPayment = dummyPaymentMapper.findById(payment.getPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR));
        PremiumGrant grant = premiumGrantService.grantLifetime(userId, paidPayment.getPaymentId());

        return PaymentResultResponse.from(paidPayment, grant);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse detail(SessionUser sessionUser, Long paymentId) {
        return detail(requireUserId(sessionUser), paymentId);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse detail(Long userId, Long paymentId) {
        requireUserId(userId);
        if (paymentId == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 ID가 필요합니다.");
        }

        DummyPayment payment = dummyPaymentMapper.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        PaymentProduct product = paymentProductMapper.findById(payment.getProductId()).orElse(null);
        PremiumGrant grant = premiumGrantService.findByPaymentId(payment.getPaymentId()).orElse(null);
        return PaymentDetailResponse.from(payment, product, grant);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResultResponse> latestResult(Long userId) {
        requireUserId(userId);
        return dummyPaymentMapper.findLatestByUserId(userId)
                .map(payment -> PaymentResultResponse.from(
                        payment,
                        premiumGrantService.findByPaymentId(payment.getPaymentId()).orElse(null)
                ));
    }

    private PaymentResultResponse reuseExistingPayment(
            DummyPayment payment,
            Long userId,
            PaymentProduct product,
            DummyPaymentForm form
    ) {
        if (!sameRequest(payment, userId, product, form)) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT);
        }

        PremiumGrant grant = premiumGrantService.findByPaymentId(payment.getPaymentId())
                .orElseGet(() -> premiumGrantService.grantLifetime(userId, payment.getPaymentId()));
        return PaymentResultResponse.from(payment, grant);
    }

    private boolean sameRequest(
            DummyPayment payment,
            Long userId,
            PaymentProduct product,
            DummyPaymentForm form
    ) {
        return userId.equals(payment.getUserId())
                && product.getProductId().equals(payment.getProductId())
                && form.getPaymentMethod().equals(payment.getPaymentMethod())
                && sameAmount(product.getPrice(), payment.getAmount())
                && STATUS_PAID.equals(payment.getPaymentStatus());
    }

    private boolean sameAmount(BigDecimal expected, BigDecimal actual) {
        return expected != null && actual != null && expected.compareTo(actual) == 0;
    }

    private String productCode(DummyPaymentForm form) {
        return hasText(form.getProductCode()) ? form.getProductCode() : DEFAULT_PRODUCT_CODE;
    }

    public String buildOrderNo(Long userId, String idempotencyToken) {
        requireUserId(userId);
        if (!hasText(idempotencyToken)) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED);
        }
        String source = userId + ":" + idempotencyToken;
        return "KNV-" + UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
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

    private void requireForm(DummyPaymentForm form) {
        if (form == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED);
        }
        if (!hasText(form.getIdempotencyToken())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED);
        }
        if (!hasText(form.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 방식이 필요합니다.");
        }
        if (!METHOD_CARD.equals(form.getPaymentMethod())
                && !METHOD_BANK_TRANSFER.equals(form.getPaymentMethod())
                && !METHOD_KAKAO_PAY.equals(form.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 방식은 CARD, BANK_TRANSFER, KAKAO_PAY만 가능합니다.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
