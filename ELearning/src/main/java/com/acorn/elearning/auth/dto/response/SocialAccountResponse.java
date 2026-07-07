package com.acorn.elearning.auth.dto.response;

import com.acorn.elearning.auth.model.SocialAccount;

import java.time.LocalDateTime;
import java.util.Map;

public record SocialAccountResponse(
        Long socialAccountId,
        String provider,
        String providerEmail,
        boolean active,
        LocalDateTime connectedAt,
        LocalDateTime disconnectedAt
) {
    //엔티티 -> 응답 DTO 변환
    public static SocialAccountResponse from(SocialAccount a) {
        return new SocialAccountResponse(
                a.getSocialAccountId(),
                a.getProvider(),
                a.getProviderEmail(),
                Boolean.TRUE.equals(a.getIsActive()), //null 안전 처리
                a.getConnectedAt(),
                a.getDisconnectedAt()
        );
    }
}
