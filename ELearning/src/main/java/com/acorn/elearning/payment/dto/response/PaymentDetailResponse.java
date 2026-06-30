package com.acorn.elearning.payment.dto.response;

import com.acorn.elearning.payment.model.DummyPayment;
import com.acorn.elearning.payment.model.PaymentProduct;
import com.acorn.elearning.payment.model.PremiumGrant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDetailResponse(
        Long paymentId,
        String orderNo,
        String productCode,
        String productName,
        String paymentMethod,
        String paymentStatus,
        BigDecimal amount,
        LocalDateTime paidAt,
        boolean premiumActive,
        Long grantId,
        String grantType,
        LocalDateTime grantedAt,
        LocalDateTime expiresAt
) {
    public static PaymentDetailResponse from(
            DummyPayment payment,
            PaymentProduct product,
            PremiumGrant grant
    ) {
        return new PaymentDetailResponse(
                payment.getPaymentId(),
                payment.getOrderNo(),
                product == null ? null : product.getProductCode(),
                product == null ? null : product.getProductName(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getPaidAt(),
                grant != null && "ACTIVE".equals(grant.getStatus()),
                grant == null ? null : grant.getGrantId(),
                grant == null ? null : grant.getGrantType(),
                grant == null ? null : grant.getGrantedAt(),
                grant == null ? null : grant.getExpiresAt()
        );
    }
}
