package com.acorn.elearning.auth.mapper;

import com.acorn.elearning.auth.model.SocialAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface SocialAccountMapper {
    Optional<SocialAccount> findById(Long socialAccountId);
    Optional<SocialAccount> findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    List<SocialAccount> findByUserId(Long userId);
    List<SocialAccount> findAll();
    int insert(SocialAccount model);
    int update(SocialAccount model);
}
