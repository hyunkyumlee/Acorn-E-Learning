package com.acorn.elearning.learning.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 온보딩 위저드 제출 폼.
 * - subjectId    : 첫 과목(primary_subject_id 대상). 위저드 "과목 선택" 단계 값.
 * - learningGoal : 학습 목표(learning_goal 대상). "목표" 단계 값.
 * - startMode    : 출발 방식. scan=레벨 스캔(레벨 테스트로 이동) / basic=기초부터(BRONZE로 시작).
 */
@Getter
@Setter
public class OnboardingForm {

    @NotNull
    private Long subjectId;

    private String learningGoal;

    private String startMode = "scan";
}
