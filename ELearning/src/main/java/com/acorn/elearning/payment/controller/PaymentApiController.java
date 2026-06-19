package com.acorn.elearning.payment.controller;

import com.acorn.elearning.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentApiController {

    @GetMapping("/api/payments/products")
    public ApiResponse<Map<String, Object>> products() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentProductListResponse response = paymentAccessService.products(sessionUser);
        // return ApiResponse.success(response);
        return ok("PAY-001");
    }

    @PostMapping("/api/payments/dummy")
    public ApiResponse<Map<String, Object>> dummy() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // DummyPaymentForm form = request body 또는 form binding 값으로 받으세요.
        // PaymentResultResponse response = dummyPaymentService.dummy(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("PAY-002");
    }

    @GetMapping("/api/payments/{paymentId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long paymentId) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PaymentDetailResponse response = dummyPaymentService.detail(sessionUser, paymentId);
        // return ApiResponse.success(response);
        return ok("PAY-003");
    }

    @GetMapping("/api/payments/premium-access")
    public ApiResponse<Map<String, Object>> premiumAccess() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PremiumAccessResponse response = paymentAccessService.premiumAccess(sessionUser);
        // return ApiResponse.success(response);
        return ok("PAY-003");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}
