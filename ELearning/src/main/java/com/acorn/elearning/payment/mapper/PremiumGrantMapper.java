package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PremiumGrant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface PremiumGrantMapper {
    Optional<PremiumGrant> findById(Long id);
    Optional<PremiumGrant> findByPaymentId(Long paymentId);
    Optional<PremiumGrant> findActiveByUserId(Long userId);
    List<PremiumGrant> findAll();
    List<PremiumGrant> findByUserId(Long userId);
    int insert(PremiumGrant model);
    int insertLifetimeGrant(PremiumGrant model);
    int revokeByPaymentIdAndUserId(
            @Param("paymentId") Long paymentId,
            @Param("userId") Long userId,
            @Param("revokeReason") String revokeReason
    );
    int update(PremiumGrant model);
}
