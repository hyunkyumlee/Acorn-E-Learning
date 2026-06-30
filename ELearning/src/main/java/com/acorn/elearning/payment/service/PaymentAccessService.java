package com.acorn.elearning.payment.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.dto.response.PaymentProductListResponse;
import com.acorn.elearning.payment.dto.response.PremiumAccessResponse;
import com.acorn.elearning.payment.mapper.PaymentProductMapper;
import com.acorn.elearning.payment.model.PremiumGrant;
import com.acorn.elearning.security.SessionUser;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PaymentAccessService {
    private final PaymentProductMapper paymentProductMapper;
    private final PremiumGrantService premiumGrantService;

    public PaymentAccessService(
            PaymentProductMapper paymentProductMapper,
            PremiumGrantService premiumGrantService
    ) {
        this.paymentProductMapper = paymentProductMapper;
        this.premiumGrantService = premiumGrantService;
    }

    public PaymentProductListResponse products() {
        return PaymentProductListResponse.from(paymentProductMapper.findActiveProducts());
    }

    public PremiumAccessResponse premiumAccess(SessionUser sessionUser) {
        return premiumAccess(requireUserId(sessionUser));
    }

    public PremiumAccessResponse premiumAccess(Long userId) {
        return premiumGrantService.findActiveByUserId(userId)
                .map(PremiumAccessResponse::active)
                .orElseGet(PremiumAccessResponse::inactive);
    }

    public boolean hasPremiumAccess(SessionUser sessionUser) {
        return hasPremiumAccess(requireUserId(sessionUser));
    }

    public boolean hasPremiumAccess(Long userId) {
        return premiumGrantService.hasActiveGrant(userId);
    }

    public PremiumGrant requirePremiumAccess(SessionUser sessionUser) {
        return requirePremiumAccess(requireUserId(sessionUser));
    }

    public PremiumGrant requirePremiumAccess(Long userId) {
        return premiumGrantService.requireActiveGrant(userId);
    }

    public Optional<PremiumGrant> findActiveGrant(Long userId) {
        return premiumGrantService.findActiveByUserId(userId);
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}
