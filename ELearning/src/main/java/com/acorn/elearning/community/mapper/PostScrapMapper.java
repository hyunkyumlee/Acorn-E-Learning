package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostScrap;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface PostScrapMapper {
    Optional<PostScrap> findById(@Param("id") Long id);
    Optional<PostScrap> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    List<PostScrap> findAll();
    List<CommunityPost> findPostsByUserId(@Param("userId") Long userId);
    int insert(PostScrap model);
    int update(PostScrap model);
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
