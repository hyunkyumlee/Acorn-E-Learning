package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.UserLearningProfile;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserLearningProfileMapper {
    Optional<UserLearningProfile> findById(@Param("id") Long id);
    Optional<UserLearningProfile> findByUserId(@Param("userId") Long userId);
    List<UserLearningProfile> findAll();
    int insert(UserLearningProfile model);
    int update(UserLearningProfile model);
}
