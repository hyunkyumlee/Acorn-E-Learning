package com.acorn.elearning.payment.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.payment.dto.request.CreateDummyPaymentRequest;
import com.acorn.elearning.payment.dto.response.PaymentDetailResponse;
import com.acorn.elearning.payment.dto.response.PaymentProductListResponse;
import com.acorn.elearning.payment.dto.response.PaymentResultResponse;
import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.service.DummyPaymentService;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentApiController {
    private final PaymentAccessService paymentAccessService;
    private final DummyPaymentService dummyPaymentService;

    public PaymentApiController(
            PaymentAccessService paymentAccessService,
            DummyPaymentService dummyPaymentService
    ) {
        this.paymentAccessService = paymentAccessService;
        this.dummyPaymentService = dummyPaymentService;
    }

    @GetMapping("/api/payments/products")
    public ApiResponse<PaymentProductListResponse> products(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        requireSessionUser(sessionUser);
        return ApiResponse.success(paymentAccessService.products());
    }

    @PostMapping("/api/payments/dummy")
    public ApiResponse<PaymentResultResponse> dummy(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody CreateDummyPaymentRequest request
    ) {
        return ApiResponse.success(dummyPaymentService.pay(requireSessionUser(sessionUser), request.toForm()));
    }

    @GetMapping("/api/payments/premium-access")
    public ApiResponse<PremiumAccessResponse> premiumAccess(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(paymentAccessService.premiumAccess(requireSessionUser(sessionUser)));
    }

    @GetMapping("/api/payments/{paymentId}")
    public ApiResponse<PaymentDetailResponse> detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long paymentId
    ) {
        return ApiResponse.success(dummyPaymentService.detail(requireSessionUser(sessionUser), paymentId));
    }

    private SessionUser requireSessionUser(SessionUser sessionUser) {
        if (sessionUser != null && sessionUser.userId() != null) {
            return sessionUser;
        }
        throw new BusinessException(ErrorCode.AUTH_REQUIRED);
    }
}
