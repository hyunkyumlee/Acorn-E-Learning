    package com.acorn.elearning.community.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class PostAttachment {
        private Long attachmentId;
private Long postId;
private Long uploaderId;
private String originalName;
private String storedName;
private String filePath;
private Long fileSize;
private LocalDateTime createdAt;
    }
