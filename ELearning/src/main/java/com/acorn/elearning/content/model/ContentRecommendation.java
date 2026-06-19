    package com.acorn.elearning.content.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class ContentRecommendation {
        private Long contentId;
private Long subjectId;
private String title;
private String url;
private String contentType;
private String recommendationSlot;
private Boolean isActive;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
