package com.acorn.elearning.user.mapper;

import com.acorn.elearning.user.model.User;
import java.util.List;
import java.util.Optional;

public interface UserMapper {
    Optional<User> findById(Long id);
    List<User> findAll();
    int insert(User model);
    int update(User model);
}
