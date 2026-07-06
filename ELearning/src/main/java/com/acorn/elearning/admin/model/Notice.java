    package com.acorn.elearning.admin.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class Notice {
        private Long noticeId;
        private Long writerId;
        private String writerNickname;
        private String title;
        private String content;
        private String status;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
