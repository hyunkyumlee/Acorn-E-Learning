    package com.acorn.elearning.community.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class CommunityPost {
        private Long postId;
private Long writerId;
private Long subjectId;
private String boardType;
private String title;
private String content;
private Integer viewCount;
private Integer likeCount;
private Integer commentCount;
private Integer scrapCount;
private boolean popular;
private String popularLabel;
private String status;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private LocalDateTime deletedAt;
    }
