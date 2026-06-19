package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.PostLike;
import java.util.List;
import java.util.Optional;

public interface PostLikeMapper {
    Optional<PostLike> findById(Long id);
    List<PostLike> findAll();
    int insert(PostLike model);
    int update(PostLike model);
}
