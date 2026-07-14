package com.acorn.elearning.payment.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.config.KakaoPayProperties;
import com.acorn.elearning.payment.dto.kakao.KakaoPayApproveResponse;
import com.acorn.elearning.payment.dto.kakao.KakaoPayReadyResponse;
import com.acorn.elearning.payment.dto.response.PaymentResultResponse;
import com.acorn.elearning.payment.form.DummyPaymentForm;
import com.acorn.elearning.payment.mapper.PaymentProductMapper;
import com.acorn.elearning.payment.model.PaymentProduct;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class KakaoPayService {
    private static final String SESSION_KEY = "KAKAO_PAY_READY";
    private static final String READY_PATH = "/online/v1/payment/ready";
    private static final String APPROVE_PATH = "/online/v1/payment/approve";
    private static final String CANCEL_PATH = "/online/v1/payment/cancel";
    private static final String PG_PROVIDER = "KAKAO_PAY";
    private static final int QUANTITY = 1;
    private static final int ZERO_AMOUNT = 0;

    private final KakaoPayProperties kakaoPayProperties;
    private final PaymentProductMapper paymentProductMapper;
    private final DummyPaymentService dummyPaymentService;
    private final PremiumGrantService premiumGrantService;
    private final RestClient restClient;
    private final String appBaseUrl;

    public KakaoPayService(
            KakaoPayProperties kakaoPayProperties,
            PaymentProductMapper paymentProductMapper,
            DummyPaymentService dummyPaymentService,
            PremiumGrantService premiumGrantService,
            @Value("${knowva.app.base-url}") String appBaseUrl
    ) {
        this.kakaoPayProperties = kakaoPayProperties;
        this.paymentProductMapper = paymentProductMapper;
        this.dummyPaymentService = dummyPaymentService;
        this.premiumGrantService = premiumGrantService;
        this.restClient = RestClient.create();
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
    }

    public String ready(SessionUser sessionUser, DummyPaymentForm form, HttpSession httpSession) {
        Long userId = requireUserId(sessionUser);
        requireText(form.getIdempotencyToken(), ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED, "중복 방지 토큰이 필요합니다.");
        requireKakaoPaySecret();

        if (premiumGrantService.hasActiveGrant(userId)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미 Premium 권한이 활성화되어 있습니다.");
        }

        PaymentProduct product = paymentProductMapper.findByCode(productCode(form))
                .filter(productItem -> Boolean.TRUE.equals(productItem.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "활성화된 결제 상품을 찾을 수 없습니다."));

        String orderNo = dummyPaymentService.buildOrderNo(userId, form.getIdempotencyToken());
        KakaoPayReadyResponse response = postReady(userId, product, orderNo);
        String redirectUrl = response == null ? null : response.redirectUrl();
        requireText(response == null ? null : response.tid(), ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 결제 고유번호를 받지 못했습니다.");
        requireText(redirectUrl, ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 결제창 URL을 받지 못했습니다.");

        httpSession.setAttribute(SESSION_KEY, new ReadySession(
                response.tid(),
                orderNo,
                userId,
                product.getProductCode(),
                form.getIdempotencyToken()
        ));
        return redirectUrl;
    }

    public PaymentResultResponse approve(SessionUser sessionUser, String pgToken, HttpSession httpSession) {
        Long userId = requireUserId(sessionUser);
        requireText(pgToken, ErrorCode.COMMON_VALIDATION_FAILED, "카카오페이 승인 토큰이 필요합니다.");

        ReadySession readySession = loadReadySession(httpSession);
        if (!userId.equals(readySession.userId())) {
            clear(httpSession);
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "결제 요청 사용자와 승인 사용자가 다릅니다.");
        }

        KakaoPayApproveResponse approveResponse = postApprove(readySession, pgToken);
        requireText(approveResponse == null ? null : approveResponse.aid(), ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 승인 응답이 올바르지 않습니다.");

        DummyPaymentForm paymentForm = new DummyPaymentForm();
        paymentForm.setProductCode(readySession.productCode());
        paymentForm.setPaymentMethod(DummyPaymentService.METHOD_KAKAO_PAY);
        paymentForm.setMemo("카카오페이 단건 결제 승인");
        paymentForm.setIdempotencyToken(readySession.idempotencyToken());

        PaymentResultResponse result = dummyPaymentService.pay(
                userId,
                paymentForm,
                PG_PROVIDER,
                approveResponse.tid()
        );
        clear(httpSession);
        return result;
    }

    public String cancel(String tid, BigDecimal refundAmount) {
        requireKakaoPaySecret();
        requireText(tid, ErrorCode.COMMON_VALIDATION_FAILED, "카카오페이 결제 고유번호가 필요합니다.");
        if (refundAmount == null || refundAmount.signum() <= 0) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "환불 금액이 필요합니다.");
        }

        int amount = amount(refundAmount);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cid", kakaoPayProperties.getCid());
        body.put("tid", tid);
        body.put("cancel_amount", amount);
        body.put("cancel_tax_free_amount", ZERO_AMOUNT);

        KakaoPayCancelResponse response = post(CANCEL_PATH, body, KakaoPayCancelResponse.class);
        boolean valid = response != null
                && "CANCEL_PAYMENT".equals(response.status());
        if (!valid) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 취소 응답이 결제 정보와 일치하지 않습니다.");
        }

        String refundTransactionId = response.aid();
        requireText(refundTransactionId, ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 취소 거래번호를 받지 못했습니다.");
        return refundTransactionId;
    }

    public void clear(HttpSession httpSession) {
        httpSession.removeAttribute(SESSION_KEY);
    }

    private KakaoPayReadyResponse postReady(Long userId, PaymentProduct product, String orderNo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cid", kakaoPayProperties.getCid());
        body.put("partner_order_id", orderNo);
        body.put("partner_user_id", partnerUserId(userId));
        body.put("item_name", product.getProductName());
        body.put("item_code", product.getProductCode());
        body.put("quantity", QUANTITY);
        body.put("total_amount", amount(product.getPrice()));
        body.put("vat_amount", ZERO_AMOUNT);
        body.put("tax_free_amount", ZERO_AMOUNT);
        body.put("approval_url", callbackUrl("/payments/kakao-pay/success"));
        body.put("cancel_url", callbackUrl("/payments/kakao-pay/cancel"));
        body.put("fail_url", callbackUrl("/payments/kakao-pay/fail"));

        return post(READY_PATH, body, KakaoPayReadyResponse.class);
    }

    private KakaoPayApproveResponse postApprove(ReadySession readySession, String pgToken) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cid", kakaoPayProperties.getCid());
        body.put("tid", readySession.tid());
        body.put("partner_order_id", readySession.orderNo());
        body.put("partner_user_id", partnerUserId(readySession.userId()));
        body.put("pg_token", pgToken);

        return post(APPROVE_PATH, body, KakaoPayApproveResponse.class);
    }

    private <T> T post(String path, Map<String, Object> body, Class<T> responseType) {
        try {
            return restClient.post()
                    .uri(apiUrl(path))
                    .header(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + kakaoPayProperties.getSecretKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException ex) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 API 요청에 실패했습니다. status=" + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "카카오페이 API와 통신하지 못했습니다.");
        }
    }

    private ReadySession loadReadySession(HttpSession httpSession) {
        Object value = httpSession.getAttribute(SESSION_KEY);
        if (value instanceof ReadySession readySession) {
            return readySession;
        }
        throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "진행 중인 카카오페이 결제 요청이 없습니다.");
    }

    private String apiUrl(String path) {
        return trimTrailingSlash(kakaoPayProperties.getBaseUrl()) + path;
    }

    private String callbackUrl(String path) {
        return appBaseUrl + path;
    }

    private String partnerUserId(Long userId) {
        return "KNOWVA_USER_" + userId;
    }

    private String productCode(DummyPaymentForm form) {
        return hasText(form.getProductCode()) ? form.getProductCode() : "PREMIUM_LIFETIME";
    }

    private int amount(BigDecimal price) {
        if (price == null) {
            return ZERO_AMOUNT;
        }
        return price.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private void requireKakaoPaySecret() {
        requireText(kakaoPayProperties.getSecretKey(), ErrorCode.COMMON_VALIDATION_FAILED, "KAKAOPAY_SECRET_KEY 환경변수가 필요합니다.");
    }

    private void requireText(String value, ErrorCode errorCode, String message) {
        if (!hasText(value)) {
            throw new BusinessException(errorCode, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private record ReadySession(
            String tid,
            String orderNo,
            Long userId,
            String productCode,
            String idempotencyToken
    ) implements Serializable {}

    private record KakaoPayCancelResponse(
            String aid,
            String status
    ) {}
}
