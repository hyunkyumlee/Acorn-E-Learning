package com.acorn.elearning.payment.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.config.TossPaymentsProperties;
import com.acorn.elearning.payment.dto.request.CreateTossPaymentOrderRequest;
import com.acorn.elearning.payment.dto.response.PaymentResultResponse;
import com.acorn.elearning.payment.dto.response.TossPaymentOrderResponse;
import com.acorn.elearning.payment.dto.toss.TossPaymentApproveResponse;
import com.acorn.elearning.payment.mapper.DummyPaymentMapper;
import com.acorn.elearning.payment.mapper.PaymentProductMapper;
import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PaymentProduct;
import com.acorn.elearning.payment.model.PremiumGrant;
import com.acorn.elearning.security.SessionUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class TossPaymentService {
    private static final String PG_PROVIDER = "TOSS_PAYMENTS";
    private static final String PAYMENT_STATUS_PENDING = "PENDING";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_FAILED = "FAILED";
    private static final String PAYMENT_STATUS_CANCELED = "CANCELED";
    private static final String TOSS_STATUS_DONE = "DONE";
    private static final String TOSS_STATUS_CANCELED = "CANCELED";
    private static final String CONFIRM_PATH = "/v1/payments/confirm";
    private static final String CANCEL_PATH = "/v1/payments/";

    private final DummyPaymentMapper dummyPaymentMapper;
    private final PaymentProductMapper paymentProductMapper;
    private final DummyPaymentService dummyPaymentService;
    private final PremiumGrantService premiumGrantService;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final RestClient restClient;

    public TossPaymentService(
            DummyPaymentMapper dummyPaymentMapper,
            PaymentProductMapper paymentProductMapper,
            DummyPaymentService dummyPaymentService,
            PremiumGrantService premiumGrantService,
            TossPaymentsProperties tossPaymentsProperties
    ) {
        this.dummyPaymentMapper = dummyPaymentMapper;
        this.paymentProductMapper = paymentProductMapper;
        this.dummyPaymentService = dummyPaymentService;
        this.premiumGrantService = premiumGrantService;
        this.tossPaymentsProperties = tossPaymentsProperties;
        this.restClient = RestClient.create();
    }

    @Transactional
    public TossPaymentOrderResponse prepare(
            SessionUser sessionUser,
            CreateTossPaymentOrderRequest request
    ) {
        Long userId = requireUserId(sessionUser);
        requireRequest(request);

        if (premiumGrantService.hasActiveGrant(userId)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미 Premium 권한이 활성화되어 있습니다.");
        }

        PaymentProduct product = paymentProductMapper.findByCode(request.productCode())
                .filter(productItem -> Boolean.TRUE.equals(productItem.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "활성화된 결제 상품을 찾을 수 없습니다."));
        String orderNo = dummyPaymentService.buildOrderNo(userId, request.idempotencyToken());

        DummyPayment payment = dummyPaymentMapper.findByOrderNoForUpdate(orderNo)
                .map(existing -> reusePendingOrder(existing, userId, product))
                .orElseGet(() -> createPendingOrder(userId, product, orderNo));

        return new TossPaymentOrderResponse(
                payment.getPaymentId(),
                payment.getOrderNo(),
                product.getProductName(),
                amount(product.getPrice()),
                UUID.randomUUID().toString()
        );
    }

    @Transactional
    public PaymentResultResponse approve(
            SessionUser sessionUser,
            String paymentKey,
            String orderId,
            Integer callbackAmount
    ) {
        Long userId = requireUserId(sessionUser);
        requireApprovalRequest(paymentKey, orderId, callbackAmount);

        DummyPayment payment = loadOwnedTossPaymentForUpdate(userId, orderId);
        if (PAYMENT_STATUS_PAID.equals(payment.getPaymentStatus())) {
            return existingPaidResult(payment, userId, paymentKey);
        }
        if (!PAYMENT_STATUS_PENDING.equals(payment.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "처리할 수 없는 결제 상태입니다.");
        }

        int expectedAmount = amount(payment.getAmount());
        if (callbackAmount != expectedAmount) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 금액이 주문 정보와 일치하지 않습니다.");
        }

        TossPaymentApproveResponse approved = confirm(paymentKey, orderId, expectedAmount);
        validateApprovedResponse(approved, paymentKey, orderId, expectedAmount);

        if (dummyPaymentMapper.markPaid(payment.getPaymentId(), PG_PROVIDER, approved.paymentKey()) != 1) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "결제 승인 상태를 변경하지 못했습니다.");
        }

        DummyPayment paidPayment = dummyPaymentMapper.findById(payment.getPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR));
        PremiumGrant grant = premiumGrantService.findByPaymentId(paidPayment.getPaymentId())
                .orElseGet(() -> premiumGrantService.grantLifetime(userId, paidPayment.getPaymentId()));
        return PaymentResultResponse.from(paidPayment, grant);
    }

    @Transactional
    public void handleFailure(SessionUser sessionUser, String orderId, String code) {
        Long userId = requireUserId(sessionUser);
        requireText(orderId, ErrorCode.COMMON_VALIDATION_FAILED, "주문번호가 필요합니다.");

        DummyPayment payment = loadOwnedTossPaymentForUpdate(userId, orderId);
        if (!PAYMENT_STATUS_PENDING.equals(payment.getPaymentStatus())) {
            return;
        }

        if (isCanceled(code)) {
            dummyPaymentMapper.markCanceled(payment.getPaymentId());
            return;
        }
        dummyPaymentMapper.markFailed(payment.getPaymentId());
    }

    public String cancel(
            String paymentKey,
            BigDecimal refundAmount,
            String refundReason,
            String idempotencyKey
    ) {
        requireText(paymentKey, ErrorCode.COMMON_VALIDATION_FAILED, "토스페이먼츠 결제키가 필요합니다.");
        requireText(refundReason, ErrorCode.COMMON_VALIDATION_FAILED, "환불 사유가 필요합니다.");
        requireText(idempotencyKey, ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED, "환불 중복 방지 키가 필요합니다.");
        int amount = refundAmount(refundAmount);
        TossPaymentCancelResponse response = postCancel(paymentKey, amount, refundReason, idempotencyKey);
        return cancelTransactionId(response, paymentKey);
    }

    private DummyPayment createPendingOrder(Long userId, PaymentProduct product, String orderNo) {
        DummyPayment payment = new DummyPayment();
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setProductId(product.getProductId());
        payment.setPaymentMethod(DummyPaymentService.METHOD_CARD);
        payment.setPgProvider(PG_PROVIDER);
        payment.setAmount(product.getPrice());
        dummyPaymentMapper.insertPending(payment);
        return payment;
    }

    private PaymentResultResponse existingPaidResult(DummyPayment payment, Long userId, String paymentKey) {
        if (!paymentKey.equals(payment.getPgTransactionId())) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 다른 결제 승인 정보가 처리되었습니다.");
        }
        PremiumGrant grant = premiumGrantService.findByPaymentId(payment.getPaymentId())
                .orElseGet(() -> premiumGrantService.grantLifetime(userId, payment.getPaymentId()));
        return PaymentResultResponse.from(payment, grant);
    }

    private DummyPayment loadOwnedTossPaymentForUpdate(Long userId, String orderId) {
        DummyPayment payment = dummyPaymentMapper.findByOrderNoForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "결제 주문을 찾을 수 없습니다."));
        boolean ownedTossCardPayment = userId.equals(payment.getUserId())
                && DummyPaymentService.METHOD_CARD.equals(payment.getPaymentMethod())
                && PG_PROVIDER.equals(payment.getPgProvider());
        if (!ownedTossCardPayment) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "해당 결제 주문을 처리할 권한이 없습니다.");
        }
        return payment;
    }

    private TossPaymentApproveResponse confirm(String paymentKey, String orderId, int amount) {
        requireText(tossPaymentsProperties.getSecretKey(), ErrorCode.COMMON_VALIDATION_FAILED, "TOSS_PAYMENTS_SECRET_KEY 환경변수가 필요합니다.");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        try {
            return restClient.post()
                    .uri(apiUrl(CONFIRM_PATH))
                    .header(HttpHeaders.AUTHORIZATION, basicAuthorization())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossPaymentApproveResponse.class);
        } catch (RestClientResponseException ex) {
            throw new BusinessException(
                    ErrorCode.COMMON_INTERNAL_ERROR,
                    "토스페이먼츠 결제 승인 요청에 실패했습니다. status=" + ex.getStatusCode().value()
            );
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "토스페이먼츠 승인 API와 통신하지 못했습니다.");
        }
    }

    private TossPaymentCancelResponse postCancel(String paymentKey, int amount, String refundReason, String idempotencyKey) {
        requireText(tossPaymentsProperties.getSecretKey(), ErrorCode.COMMON_VALIDATION_FAILED, "TOSS_PAYMENTS_SECRET_KEY 환경변수가 필요합니다.");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cancelReason", refundReason);
        body.put("cancelAmount", amount);

        try {
            return restClient.post()
                    .uri(apiUrl(CANCEL_PATH + paymentKey + "/cancel"))
                    .header(HttpHeaders.AUTHORIZATION, basicAuthorization())
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossPaymentCancelResponse.class);
        } catch (RestClientResponseException ex) {
            throw new BusinessException(
                    ErrorCode.COMMON_INTERNAL_ERROR,
                    "토스페이먼츠 결제 취소 요청에 실패했습니다. status=" + ex.getStatusCode().value()
            );
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "토스페이먼츠 취소 API와 통신하지 못했습니다.");
        }
    }

    private void validateApprovedResponse(
            TossPaymentApproveResponse response,
            String paymentKey,
            String orderId,
            int expectedAmount
    ) {
        boolean valid = response != null
                && paymentKey.equals(response.paymentKey())
                && orderId.equals(response.orderId())
                && TOSS_STATUS_DONE.equals(response.status())
                && response.totalAmount() != null
                && expectedAmount == response.totalAmount();
        if (!valid) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "토스페이먼츠 승인 응답이 주문 정보와 일치하지 않습니다.");
        }
    }

    private String cancelTransactionId(TossPaymentCancelResponse response, String paymentKey) {
        if (response == null
                || !paymentKey.equals(response.paymentKey())
                || !TOSS_STATUS_CANCELED.equals(response.status())) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "토스페이먼츠 취소 응답이 결제 정보와 일치하지 않습니다.");
        }

        String transactionId = response.lastTransactionKey();
        requireText(transactionId, ErrorCode.COMMON_INTERNAL_ERROR, "토스페이먼츠 취소 거래번호를 받지 못했습니다.");
        return transactionId;
    }

    private String basicAuthorization() {
        String credential = tossPaymentsProperties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String apiUrl(String path) {
        String baseUrl = tossPaymentsProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "TOSS_PAYMENTS_BASE_URL 환경변수가 필요합니다.");
        }
        return baseUrl.replaceAll("/+$", "") + path;
    }

    private DummyPayment reusePendingOrder(DummyPayment payment, Long userId, PaymentProduct product) {
        boolean samePendingOrder = userId.equals(payment.getUserId())
                && product.getProductId().equals(payment.getProductId())
                && DummyPaymentService.METHOD_CARD.equals(payment.getPaymentMethod())
                && PG_PROVIDER.equals(payment.getPgProvider())
                && sameAmount(product.getPrice(), payment.getAmount())
                && PAYMENT_STATUS_PENDING.equals(payment.getPaymentStatus());
        if (!samePendingOrder) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT);
        }
        return payment;
    }

    private int amount(BigDecimal price) {
        if (price == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 금액이 필요합니다.");
        }
        try {
            return price.setScale(0, RoundingMode.UNNECESSARY).intValueExact();
        } catch (ArithmeticException ex) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 금액은 원 단위 정수여야 합니다.");
        }
    }

    private int refundAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "환불 금액이 필요합니다.");
        }
        return amount(amount);
    }

    private boolean sameAmount(BigDecimal expected, BigDecimal actual) {
        return expected != null && actual != null && expected.compareTo(actual) == 0;
    }

    private void requireApprovalRequest(String paymentKey, String orderId, Integer callbackAmount) {
        requireText(paymentKey, ErrorCode.COMMON_VALIDATION_FAILED, "토스페이먼츠 결제키가 필요합니다.");
        requireText(orderId, ErrorCode.COMMON_VALIDATION_FAILED, "주문번호가 필요합니다.");
        if (callbackAmount == null || callbackAmount < 0) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 금액이 필요합니다.");
        }
    }

    private void requireText(String value, ErrorCode errorCode, String message) {
        if (!hasText(value)) {
            throw new BusinessException(errorCode, message);
        }
    }

    private boolean isCanceled(String code) {
        return "PAY_PROCESS_CANCELED".equals(code) || "USER_CANCEL".equals(code);
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private void requireRequest(CreateTossPaymentOrderRequest request) {
        if (request == null || !hasText(request.productCode()) || !hasText(request.idempotencyToken())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "상품과 중복 방지 토큰이 필요합니다.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record TossPaymentCancelResponse(
            String paymentKey,
            String status,
            String lastTransactionKey
    ) {}
}
