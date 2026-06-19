    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class Subject {
        private Long subjectId;
private String subjectCode;
private String subjectName;
private String description;
private Integer sortOrder;
private Boolean isActive;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
