package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminCommunityPageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminCommunityMapper {

    List<AdminCommunityPageResponse.PostItem> findPosts();
    List<AdminCommunityPageResponse.CommentItem> findComments();
    int updatePostStatus(@Param("postId") Long postId, @Param("status") String status);
    int updateCommentStatus(@Param("commentId") Long commentId, @Param("status") String status);
}
