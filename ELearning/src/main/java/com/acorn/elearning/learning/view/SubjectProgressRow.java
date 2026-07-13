package com.acorn.elearning.learning.view;

import lombok.Getter;
import lombok.Setter;

/** 과목별 레슨 진행 집계 결과(과목 하나당 한 행). */
@Getter
@Setter
public class SubjectProgressRow {

    private Long subjectId;
    private int totalLessons;
    private int completedLessons;
}
