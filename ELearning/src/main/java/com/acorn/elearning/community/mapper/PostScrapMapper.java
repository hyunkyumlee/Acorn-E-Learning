package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.PostScrap;
import java.util.List;
import java.util.Optional;

public interface PostScrapMapper {
    Optional<PostScrap> findById(Long id);
    List<PostScrap> findAll();
    int insert(PostScrap model);
    int update(PostScrap model);
}
