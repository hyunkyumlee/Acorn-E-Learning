package com.acorn.elearning.learning.view;

/**
 * 학습 메인 좌측 과목 목록의 과목 한 칸.
 *
 * @param enrolled         수강 중이면 true. false면 잠금으로 표시하고 과목 소개 화면으로 보낸다.
 * @param selected         현재 로드맵이 그리고 있는 과목
 * @param currentLevelCode 해당 과목에서 연 가장 높은 레벨(연 레벨이 없으면 null)
 * @param progressPercent  해당 과목 전체 required 레슨 대비 완료 비율(0~100)
 */
public record SubjectCardView(
        Long subjectId,
        String subjectCode,
        String subjectName,
        String description,
        boolean enrolled,
        boolean selected,
        String currentLevelCode,
        int progressPercent
) {}
