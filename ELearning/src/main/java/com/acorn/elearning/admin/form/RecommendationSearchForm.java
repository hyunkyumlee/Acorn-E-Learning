package com.acorn.elearning.admin.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationSearchForm {
    Long subjectId;
    String title;
    String url;
    String contentType;
    String recommendationSlot;
    Boolean isActive;
}
