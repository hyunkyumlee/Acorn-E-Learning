package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.User;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    Optional<User> findById(@Param("id") Long id);
    Optional<User> findByEmail(@Param("email") String email);
    List<User> findAll();
    int insert(User model);
    int update(User model);

    //정하 - auth 용 nickname 중복 검사 추가
    boolean existByNickname(@Param("nickname") String nickname);
}
