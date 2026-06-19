package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.UserLearningProfile;
import java.util.List;
import java.util.Optional;

public interface UserLearningProfileMapper {
    Optional<UserLearningProfile> findById(Long id);
    List<UserLearningProfile> findAll();
    int insert(UserLearningProfile model);
    int update(UserLearningProfile model);
}
