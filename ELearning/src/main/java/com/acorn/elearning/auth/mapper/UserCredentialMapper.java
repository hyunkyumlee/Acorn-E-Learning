package com.acorn.elearning.auth.mapper;

import com.acorn.elearning.auth.model.LoginUserRow;
import com.acorn.elearning.auth.model.UserCredential;
import java.util.List;
import java.util.Optional;

public interface UserCredentialMapper {
    Optional<UserCredential> findById(Long credentialId);
    Optional<UserCredential> findByUserId(Long userId);
    Optional<LoginUserRow> findByLoginEmail(String email);

    List<UserCredential> findAll();
    int insert(UserCredential model);
    int update(UserCredential model);
    // [추가] 탈퇴 시 credential 비활성 처리 — login_email 마스킹으로 이메일 로그인 차단 + login_email 점유 해제
    int deactivateByUserId(Long userId);

}
