package com.acorn.elearning.exam.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamLearningScopeItem {
    private String sourceType;
    private String nodeTitle;
    private String title;
    private String summary;
    private String exampleCode;
    private Integer sortOrder;
}
