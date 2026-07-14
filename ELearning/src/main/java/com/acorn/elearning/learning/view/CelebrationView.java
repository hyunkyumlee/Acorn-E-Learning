package com.acorn.elearning.learning.view;

/**
 * 학습 메인에서 한 번만 띄우는 축하 연출.
 * 직전에 본 로드맵 상태와 지금 상태를 비교해 새로 생긴 성취가 있을 때만 만들어진다.
 *
 * @param type       PLANET(행성 완료) 또는 LEVEL(관문을 넘어 레벨 달성)
 * @param eyebrow    카드 상단 라벨
 * @param title      성취 이름(행성 이름 또는 레벨 코드)
 * @param message    다음 행동을 알려주는 한 줄
 * @param planetFile 행성 아트 파일명(확장자 제외). LEVEL 연출에는 없어서 null.
 * @param levelCode  달성한 레벨 코드. PLANET 연출에는 표시할 레벨이 없어서 null.
 */
public record CelebrationView(
        String type,
        String eyebrow,
        String title,
        String message,
        String planetFile,
        String levelCode
) {}
