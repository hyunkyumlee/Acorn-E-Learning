package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.UserLevelUnlock;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserLevelUnlockMapper {
    Optional<UserLevelUnlock> findById(Long id);
    List<UserLevelUnlock> findAll();
    /** 중복 unlock 방지용: (user_id, subject_id, level_code) UNIQUE 키로 기존 unlock 존재 여부 조회. */
    Optional<UserLevelUnlock> findByUserSubjectLevel(@Param("userId") Long userId,
                                                     @Param("subjectId") Long subjectId,
                                                     @Param("levelCode") String levelCode);
    int insert(UserLevelUnlock model);
    int update(UserLevelUnlock model);
}
