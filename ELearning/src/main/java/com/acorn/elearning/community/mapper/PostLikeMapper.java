package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostLike;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface PostLikeMapper {
    Optional<PostLike> findById(@Param("id") Long id);
    Optional<PostLike> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    List<PostLike> findAll();
    List<CommunityPost> findPostsByUserId(@Param("userId") Long userId);
    int insert(PostLike model);
    int update(PostLike model);
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
