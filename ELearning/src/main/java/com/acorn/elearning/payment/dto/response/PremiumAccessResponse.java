package com.acorn.elearning.payment.dto.response;

import com.acorn.elearning.payment.model.PremiumGrant;
import java.time.LocalDateTime;

public record PremiumAccessResponse(
        boolean premiumActive,
        String grantType,
        LocalDateTime grantedAt,
        LocalDateTime expiresAt
) {
    public static PremiumAccessResponse active(PremiumGrant grant) {
        return new PremiumAccessResponse(true, grant.getGrantType(), grant.getGrantedAt(), grant.getExpiresAt());
    }

    public static PremiumAccessResponse inactive() {
        return new PremiumAccessResponse(false, null, null, null);
    }
}
