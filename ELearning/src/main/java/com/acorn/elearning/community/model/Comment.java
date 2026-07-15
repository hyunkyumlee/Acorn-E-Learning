    package com.acorn.elearning.community.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class Comment {
        private Long commentId;
        private Long postId;
        private Long parentCommentId;
        private Long writerId;
        private Long deletedByAdminId;
        private String content;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }
