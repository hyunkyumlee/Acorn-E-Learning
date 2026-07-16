package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.model.CommunityPost;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface CommunityPostMapper {
    Optional<CommunityPost> findById(@Param("id") Long id);
    Optional<CommunityPost> findActiveById(@Param("id") Long id);
    List<CommunityPost> findAll();
    List<CommunityPost> findPage(@Param("condition") PostSearchCondition condition);
    long countPage(@Param("condition") PostSearchCondition condition);
    List<Long> findWeeklyPopularPostIds();
    List<Long> findMonthlyPopularPostIds();
    List<CommunityPost> findByWriterId(@Param("writerId") Long writerId);
    int insert(CommunityPost model);
    int update(CommunityPost model);
    int publishDraft(CommunityPost model);
    int softDelete(@Param("postId") Long postId, @Param("writerId") Long writerId);
    int incrementLikeCount(@Param("postId") Long postId);
    int decrementLikeCount(@Param("postId") Long postId);
    int incrementScrapCount(@Param("postId") Long postId);
    int decrementScrapCount(@Param("postId") Long postId);
    int incrementCommentCount(@Param("postId") Long postId);
    int decrementCommentCount(@Param("postId") Long postId);
    int incrementViewCount(@Param("postId") Long postId);
}
