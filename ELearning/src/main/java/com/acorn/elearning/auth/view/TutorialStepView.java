package com.acorn.elearning.auth.view;

//스켈레톤 stub 제거 -> 튜토리얼 한 단계(슬라이드)를 표현하는 실제 뷰모델
//step : 단계 번호
//title/description : 슬라이드 제목/문장
//ImageUrl : 단계 이미지 경로 (없으면 Null -> 템플릿에서 placeholder)
public record TutorialStepView(int step, String title, String description, String imageUrl) {
    // 이미지 없는 단계용 편의 팩토리
    public static TutorialStepView of(int step, String title, String description) {
        return new TutorialStepView(step, title, description, null);
    }
}
