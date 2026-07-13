package com.acorn.elearning.learning.view;

import lombok.Getter;
import lombok.Setter;

/** 한 과목 안의 레벨별 레슨 진행 집계 결과(레벨 하나당 한 행). */
@Getter
@Setter
public class LevelProgressRow {

    private String levelCode;
    private int totalLessons;
    private int completedLessons;
}
