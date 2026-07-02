package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.UserSetting;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserSettingMapper {
    Optional<UserSetting> findById(@Param("id") Long id);
    Optional<UserSetting> findByUserId(@Param("userId") Long userId);
    List<UserSetting> findAll();
    int insert(UserSetting model);
    int update(UserSetting model);
}
