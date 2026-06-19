package com.acorn.elearning.payment.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DummyPaymentService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // PaymentProduct product = paymentProductMapper.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // DummyPayment payment = DummyPayment.approved(userId, product);
        // dummyPaymentMapper.insert(payment); premiumGrantMapper.insertOrExtend(payment.toPremiumGrant());
        // return Map.of("payment", PaymentResultResponse.from(payment));
        return Map.of("action", action, "status", "SKELETON");
    }
}
