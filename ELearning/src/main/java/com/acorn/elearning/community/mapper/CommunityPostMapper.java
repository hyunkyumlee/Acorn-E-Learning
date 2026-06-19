package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.CommunityPost;
import java.util.List;
import java.util.Optional;

public interface CommunityPostMapper {
    Optional<CommunityPost> findById(Long id);
    List<CommunityPost> findAll();
    int insert(CommunityPost model);
    int update(CommunityPost model);
}
