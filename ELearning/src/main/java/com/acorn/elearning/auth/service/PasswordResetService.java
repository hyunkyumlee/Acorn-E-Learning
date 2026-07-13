package com.acorn.elearning.auth.service;


import com.acorn.elearning.auth.mapper.PasswordResetTokenMapper;
import com.acorn.elearning.auth.mapper.UserCredentialMapper;
import com.acorn.elearning.auth.model.LoginUserRow;
import com.acorn.elearning.auth.model.PasswordResetToken;
import com.acorn.elearning.auth.model.UserCredential;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;


@Service
public class PasswordResetService {
    //비밀번호 찾기 요청 결과 - controller의 화면 분기용
    public enum ForgotResult {SENT, SOCIAL_ONLY}

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final int TOKEN_BYTES = 32;              // 256bit 랜덤 토큰
    private static final long RESEND_COOLDOWN_SECONDS = 60; // 재전송 쿨다운

    private final PasswordResetTokenMapper passwordResetTokenMapper;
    private final UserCredentialMapper userCredentialMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailService passwordResetMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(PasswordResetTokenMapper passwordResetTokenMapper, UserCredentialMapper userCredentialMapper, UserMapper userMapper, PasswordEncoder passwordEncoder, PasswordResetMailService passwordResetMailService) {
        this.passwordResetTokenMapper = passwordResetTokenMapper;
        this.userCredentialMapper = userCredentialMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetMailService = passwordResetMailService;
    }

    //토큰 유효시간(분). 요구사항 30분 - application.properties에서 조정 가능
    @Value("${knowva.password-reset.token-ttl-minutes:30}")
    private long tokenTtlMinutes;

    @Value("${knowva.app.base-url}")
    private String appBaseUrl;

    // 1) 비밀번호 찾기 요청 (이메일 입력 -> 메일 발송)
    @Transactional
    public ForgotResult requestReset(String email) {
        passwordResetTokenMapper.deleteExpired(); // 만료 토큰 가벼운 정리

        LoginUserRow credentialRow = userCredentialMapper.findByLoginEmail(email).orElse(null);

        if (credentialRow == null) {
            //credential row 가 없는데 users에는 있음 -> 소셜 전용 계정 (비번 자체가 없음)
            boolean socialOnly = userMapper.findByEmail(email).isPresent();
            if (socialOnly) {
                return ForgotResult.SOCIAL_ONLY;
            }
            //미가입 이메일 : 계정 존재 여부 노출 방지 -> "보냈습니다" 와 동일하게 응답
            return ForgotResult.SENT;
        }

        if (!STATUS_ACTIVE.equals(credentialRow.getStatus())) {
            // 정지/탈퇴 계정 : 메일은 보내지 않지만 응답은 동일 (계정 상태 노출 방지)
            return ForgotResult.SENT;
        }

        String token = issueToken(credentialRow.getUserId());
        if (token == null) {
            return ForgotResult.SENT; // 쿨다운 중 재요청 -> 메일 재발송만 생략
        }

        String resetUrl = appBaseUrl + "/password/reset?token=" + token;
        passwordResetMailService.sendResetLink(email, resetUrl, tokenTtlMinutes);
        return ForgotResult.SENT;
    }

    //토큰 발급 : 기존 미사용 토큰 무효화 -> 새 토큰 저장 (해시 -> 원문 반환 (메일 링크용))
    private String issueToken(Long userId) {
        PasswordResetToken latest = passwordResetTokenMapper.findLatestActiveByUserId(userId).orElse(null);
        if (latest != null && latest.getCreatedAt() != null && latest.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
            return null; // 60초 내 재요청 -> 메일 폭탄 방지
        }
        passwordResetTokenMapper.invalidateActiveByUserId(userId); // 유효 링크는 항상 최신 1개만

        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); //URL-safe 43자

        PasswordResetToken row = new  PasswordResetToken();
        row.setUserId(userId);
        row.setTokenHash(sha256Hex(token));
        row.setExpiresAt(LocalDateTime.now().plusMinutes(tokenTtlMinutes));
        passwordResetTokenMapper.insert(row);
        return token;
    }

    // 2) 재설정 화면 진입시 토큰 검증 (GET /password/reset)
    @Transactional(readOnly = true)
    public void validateToken(String token) {findValidRow(token);}

    // 3) 새 비밀번호 저장 (POST /password/reset)
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken row = findValidRow(token);

        //일회용 처리 : used_at IS NULL 조건이 걸린 update 라서 동시에 두번 제출되어도 한번만 성공
        if (passwordResetTokenMapper.markUsed(row.getTokenId()) != 1) {
            throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_USED);
        }

        UserCredential credential = userCredentialMapper.findByUserId(row.getUserId()).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_RESET_TOKEN_INVALID));
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        //update() 가 password_updated_at = NOW() 를 함께 갱신 -> remember-me 쿠키의 tokenVersion이 달라져 기존 자동로그인 쿠키가 전부 무효화 된다
        userCredentialMapper.update(credential);
    }

    //토큰 검증 공통 : 존재 -> 사용 여부 -> 만료(30분) 순서로 확인
    private PasswordResetToken findValidRow(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_INVALID);
        }
        PasswordResetToken row = passwordResetTokenMapper.findByTokenHash(sha256Hex(token)).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_RESET_TOKEN_INVALID));
        if (row.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_USED);
        }
        if (row.getExpiresAt() == null || row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_EXPIRED); //30분 경과 -> 재설정 불가
        }
        return row;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }

    }

}
