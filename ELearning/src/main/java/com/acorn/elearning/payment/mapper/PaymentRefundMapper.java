package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PaymentRefund;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface PaymentRefundMapper {
    Optional<PaymentRefund> findByPaymentId(Long paymentId);

    Optional<PaymentRefund> findByPaymentIdForUpdate(Long paymentId);

    List<PaymentRefund> findByUserId(Long userId);

    int insertPending(PaymentRefund refund);

    int markCompleted(
            @Param("refundId") Long refundId,
            @Param("pgRefundTransactionId") String pgRefundTransactionId
    );

    int markFailed(
            @Param("refundId") Long refundId,
            @Param("failureCode") String failureCode
    );
}
