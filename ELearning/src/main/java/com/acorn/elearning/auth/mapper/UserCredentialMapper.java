package com.acorn.elearning.auth.mapper;

import com.acorn.elearning.auth.model.UserCredential;
import java.util.List;
import java.util.Optional;

public interface UserCredentialMapper {
    Optional<UserCredential> findById(Long credentialId);
    Optional<UserCredential> findByUserId(Long userId);
    Optional<UserCredential> findById(String email);

    List<UserCredential> findAll();
    int insert(UserCredential model);
    int update(UserCredential model);
}
