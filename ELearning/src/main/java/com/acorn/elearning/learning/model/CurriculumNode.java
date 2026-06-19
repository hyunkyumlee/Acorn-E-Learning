    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class CurriculumNode {
        private Long nodeId;
private Long subjectId;
private Long parentNodeId;
private String levelCode;
private String nodeType;
private Integer planetNo;
private String title;
private String description;
private Integer sortOrder;
private String gateCondition;
private Boolean isActive;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
