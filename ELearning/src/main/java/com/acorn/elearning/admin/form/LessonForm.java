package com.acorn.elearning.admin.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonForm {
    private Long lessonId;
    private Long nodeId;
    private Boolean requiredForCompletion;
    private String title;
    private String content;
    private Integer sortOrder;
    private Boolean isActive;
}
