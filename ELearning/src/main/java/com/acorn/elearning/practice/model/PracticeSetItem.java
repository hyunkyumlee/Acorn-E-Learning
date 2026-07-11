package com.acorn.elearning.practice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeSetItem {

    private Long setItemId;
    private Long setAttemptId;
    private Long problemId;
    private Integer sortOrder;

}
