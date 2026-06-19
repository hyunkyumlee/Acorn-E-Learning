package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.UserLevelUnlock;
import java.util.List;
import java.util.Optional;

public interface UserLevelUnlockMapper {
    Optional<UserLevelUnlock> findById(Long id);
    List<UserLevelUnlock> findAll();
    int insert(UserLevelUnlock model);
    int update(UserLevelUnlock model);
}
