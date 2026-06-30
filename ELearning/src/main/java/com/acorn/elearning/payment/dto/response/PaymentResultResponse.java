package com.acorn.elearning.payment.dto.response;

import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PremiumGrant;

public record PaymentResultResponse(
        Long paymentId,
        String orderNo,
        String paymentStatus,
        boolean premiumActive,
        Long grantId
) {
    public static PaymentResultResponse from(DummyPayment payment, PremiumGrant grant) {
        return new PaymentResultResponse(
                payment.getPaymentId(),
                payment.getOrderNo(),
                payment.getPaymentStatus(),
                grant != null && "ACTIVE".equals(grant.getStatus()),
                grant == null ? null : grant.getGrantId()
        );
    }
}
