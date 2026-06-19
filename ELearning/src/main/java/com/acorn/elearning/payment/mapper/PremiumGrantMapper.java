package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PremiumGrant;
import java.util.List;
import java.util.Optional;

public interface PremiumGrantMapper {
    Optional<PremiumGrant> findById(Long id);
    List<PremiumGrant> findAll();
    int insert(PremiumGrant model);
    int update(PremiumGrant model);
}
