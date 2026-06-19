    package com.acorn.elearning.community.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class PostScrap {
        private Long scrapId;
private Long postId;
private Long userId;
private LocalDateTime createdAt;
    }
