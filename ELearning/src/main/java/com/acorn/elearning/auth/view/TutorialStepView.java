package com.acorn.elearning.auth.view;

//스켈레톤 stub 제거 -> 튜토리얼 한 단계(슬라이드)를 표현하는 실제 뷰모델
//step : 단계 번호
//title/description : 슬라이드 제목/문장
//imageUrl : 단계 이미지 경로 (없으면 Null -> 템플릿에서 placeholder)
//nuviXPercent/nuviYPercent : tut-media 기준 누비 캐릭터 상대 좌표(0~100). null이면 누비 숨김
//nuviPose : WAVING|READING|TELESCOPE|IDEA|PAINTING. null이면 누비 숨김
//highlightLeftPercent/highlightTopPercent/highlightWidthPercent/highlightHeightPercent : 캡쳐본 기준 포인트 영역 표시 박스 좌표/크기(0~100). null이면 박스 숨김
//bubbleText : 누비 말풍선 문구. null이면 말풍선 숨김
public record TutorialStepView(
        int step,
        String title,
        String description,
        String imageUrl,
        Integer nuviXPercent,
        Integer nuviYPercent,
        String nuviPose,
        Integer highlightLeftPercent,
        Integer highlightTopPercent,
        Integer highlightWidthPercent,
        Integer highlightHeightPercent,
        String bubbleText) {

    // 이미지/누비/하이라이트 없는 단계용 편의 팩토리
    public static TutorialStepView of(int step, String title, String description) {
        return new TutorialStepView(
                step, title, description, null, null, null, null, null, null, null, null, null);
    }

    // 이미지 + 누비 좌표/포즈 + 하이라이트 박스/말풍선까지 있는 스텝용 팩토리
    public static TutorialStepView of(
            int step, String title, String description,
            String imageUrl, int nuviXPercent, int nuviYPercent, String nuviPose,
            int highlightLeftPercent, int highlightTopPercent, int highlightWidthPercent, int highlightHeightPercent,
            String bubbleText) {
        return new TutorialStepView(
                step, title, description, imageUrl, nuviXPercent, nuviYPercent, nuviPose,
                highlightLeftPercent, highlightTopPercent, highlightWidthPercent, highlightHeightPercent, bubbleText);
    }
}
