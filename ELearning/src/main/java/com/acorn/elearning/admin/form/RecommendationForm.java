package com.acorn.elearning.admin.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationForm {
    private Long contentId;
    private Long subjectId;
    private String title;
    private String url;
    private String contentType;
    private String recommendationSlot;
    private Boolean isActive;
}
