    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class Lesson {
        private Long lessonId;
private Long nodeId;
private String title;
private String summary;
private String content;
private String exampleCode;
private Integer sortOrder;
private Boolean isActive;
private Long createdBy;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
