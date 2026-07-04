package com.acorn.elearning.admin.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record AdminCommunityPageResponse(
        List<PostItem> posts,
        List<CommentItem> comments
) {
    public record PostItem(
            Long postId,
            String writerNickname,
            String title,
            String boardType,
            Integer likeCount,
            Integer commentCount,
            Integer reportCount,
            LocalDateTime createdAt,
            String status
    ) {
    }

    public record CommentItem(
            Long commentId,
            Long postId,
            String writerNickname,
            String content,
            Integer reportCount,
            LocalDateTime createdAt,
            String status
    ) {
    }
}
