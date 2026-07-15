package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminCommunityPageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface AdminCommunityMapper {

    Optional<AdminCommunityPageResponse.PostItem> findPostById(
            @Param("postId") Long postId
    );
    Optional<AdminCommunityPageResponse.CommentItem> findCommentById(
            @Param("commentId") Long commentId
    );
    List<AdminCommunityPageResponse.PostItem> findPosts();
    List<AdminCommunityPageResponse.CommentItem> findComments();
    List<AdminCommunityPageResponse.PostItem> findPostPage(@Param("limit") int limit,
                                                           @Param("offset") int offset,
                                                           @Param("boardType") String boardType,
                                                           @Param("status") String status,
                                                           @Param("keyword") String keyword);
    long countPosts(@Param("boardType") String boardType,
                    @Param("status") String status,
                    @Param("keyword") String keyword);
    List<AdminCommunityPageResponse.CommentItem> findCommentPage(@Param("limit") int limit,
                                                                 @Param("offset") int offset,
                                                                 @Param("status") String status,
                                                                 @Param("keyword") String keyword);
    long countComments(@Param("status") String status,
                       @Param("keyword") String keyword);
    int updatePostStatus(@Param("postId") Long postId, @Param("status") String status);
    int updateCommentStatus(@Param("commentId") Long commentId, @Param("status") String status, @Param("adminId") Long adminId);
}
