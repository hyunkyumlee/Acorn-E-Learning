package com.acorn.elearning.auth.mapper;

import com.acorn.elearning.auth.model.SocialAccount;
import java.util.List;
import java.util.Optional;

public interface SocialAccountMapper {
    Optional<SocialAccount> findById(Long socialAccountId);
    Optional<SocialAccount> findByProvAndProvUID(String provider, String providerUserId);
    List<SocialAccount> findbyUserId(Long userId);
    List<SocialAccount> findAll();
    int insert(SocialAccount model);
    int update(SocialAccount model);
}
