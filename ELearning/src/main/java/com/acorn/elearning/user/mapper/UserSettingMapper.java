package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.UserSetting;
import java.util.List;
import java.util.Optional;

public interface UserSettingMapper {
    Optional<UserSetting> findById(Long id);
    List<UserSetting> findAll();
    int insert(UserSetting model);
    int update(UserSetting model);
}
