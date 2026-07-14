package com.acorn.elearning.auth.mapper;

import com.acorn.elearning.auth.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenMapper {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    //재전송 쿨다운 판단용 : 가장 최근의 미사용/미만료 토큰 1건
    Optional<PasswordResetToken> findLatestActiveByUserId(Long userId);

    int insert(PasswordResetToken model);

    //일회용 처리 : used_at IS NULL 조건이 포함된 UPDATE (성공 row 수 반환)
    int markUsed(Long tokenId);

    // 새요청시 기존 미사용 토큰 전부 무효화
    int invalidateActiveByUserId(Long userId);

    //만료 후 하루 지난 토큰 정리
    int deleteExpired();
}
