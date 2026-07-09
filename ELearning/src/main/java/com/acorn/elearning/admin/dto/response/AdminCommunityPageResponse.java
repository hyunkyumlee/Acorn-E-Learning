package com.acorn.elearning.admin.dto.response;


import java.time.LocalDateTime;

public record AdminCommunityPageResponse(
        AdminPageResponse<PostItem> posts,
        AdminPageResponse<CommentItem> comments
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
        public String boardTypeLabel() {
            if ("FREE".equals(boardType)) {
                return "자유";
            }
            if ("QUESTION".equals(boardType) || "QNA".equals(boardType)) {
                return "질문";
            }
            if ("STUDY_LOG".equals(boardType)) {
                return "공부 일지";
            }
            return hasText(boardType) ? boardType : "-";
        }

        public String statusLabel() {
            return communityStatusLabel(status);
        }

        public String statusClass() {
            return communityStatusClass(status);
        }
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
        public String statusLabel() {
            return communityStatusLabel(status);
        }

        public String statusClass() {
            return communityStatusClass(status);
        }

        public String contentSummary() {
            if (!hasText(content)) {
                return "-";
            }
            return content.length() > 40 ? content.substring(0, 40) + "..." : content;
        }
    }

    private static String communityStatusLabel(String status) {
        if ("ACTIVE".equals(status)) {
            return "공개";
        }
        if ("HIDDEN".equals(status)) {
            return "숨김";
        }
        if ("DELETED".equals(status)) {
            return "삭제";
        }
        return hasText(status) ? status : "-";
    }

    private static String communityStatusClass(String status) {
        if ("ACTIVE".equals(status)) {
            return " is-active";
        }
        if ("HIDDEN".equals(status)) {
            return " is-hidden-status";
        }
        if ("DELETED".equals(status)) {
            return " is-deleted";
        }
        return " is-muted";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
