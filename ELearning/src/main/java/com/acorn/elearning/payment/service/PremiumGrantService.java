package com.acorn.elearning.payment.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.mapper.PremiumGrantMapper;
import com.acorn.elearning.payment.model.PremiumGrant;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PremiumGrantService {
    private final PremiumGrantMapper premiumGrantMapper;

    public PremiumGrantService(PremiumGrantMapper premiumGrantMapper) {
        this.premiumGrantMapper = premiumGrantMapper;
    }

    public Optional<PremiumGrant> findActiveByUserId(Long userId) {
        requireUserId(userId);
        return premiumGrantMapper.findActiveByUserId(userId);
    }

    public Optional<PremiumGrant> findActiveByUserIdForUpdate(Long userId) {
        requireUserId(userId);
        return premiumGrantMapper.findActiveByUserIdForUpdate(userId);
    }

    public Optional<PremiumGrant> findByPaymentId(Long paymentId) {
        if (paymentId == null) {
            return Optional.empty();
        }
        return premiumGrantMapper.findByPaymentId(paymentId);
    }

    public boolean hasActiveGrant(Long userId) {
        return findActiveByUserId(userId).isPresent();
    }

    public PremiumGrant requireActiveGrant(Long userId) {
        return findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_FORBIDDEN, "Premium 권한이 필요합니다."));
    }

    public PremiumGrant grantLifetime(Long userId, Long paymentId) {
        requireUserId(userId);
        if (paymentId == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "결제 ID가 필요합니다.");
        }

        PremiumGrant grant = new PremiumGrant();
        grant.setUserId(userId);
        grant.setPaymentId(paymentId);
        premiumGrantMapper.insertLifetimeGrant(grant);

        return premiumGrantMapper.findById(grant.getGrantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR));
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }
}
