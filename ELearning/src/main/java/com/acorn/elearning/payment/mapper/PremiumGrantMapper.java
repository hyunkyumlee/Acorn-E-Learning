package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PremiumGrant;
import java.util.List;
import java.util.Optional;

public interface PremiumGrantMapper {
    Optional<PremiumGrant> findById(Long id);
    Optional<PremiumGrant> findByPaymentId(Long paymentId);
    Optional<PremiumGrant> findActiveByUserId(Long userId);
    List<PremiumGrant> findAll();
    List<PremiumGrant> findByUserId(Long userId);
    int insert(PremiumGrant model);
    int insertLifetimeGrant(PremiumGrant model);
    int update(PremiumGrant model);
}
