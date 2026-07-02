package com.acorn.elearning.auth.service;

import com.acorn.elearning.auth.mapper.UserCredentialMapper;
import com.acorn.elearning.auth.model.UserCredential;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.user.service.PasswordChangePort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthPasswordChangeAdapter implements PasswordChangePort {

    private final UserCredentialMapper userCredentialMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthPasswordChangeAdapter(UserCredentialMapper userCredentialMapper, PasswordEncoder passwordEncoder) {
        this.userCredentialMapper = userCredentialMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserCredential credential = userCredentialMapper.findByUserId(userId).orElseThrow( () -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "비밀번호 정보를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(currentPassword, credential.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "현재 비밀번호가 일치하지 않습니다.");
        }
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        userCredentialMapper.update(credential);
    }
}
