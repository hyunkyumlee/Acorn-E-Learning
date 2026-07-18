package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.view.TutorialStepView;   // [추가]
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;   // [추가]

@Controller
public class WelcomeController {

    // [추가] 튜토리얼 단계 정의 — 지금은 상수. 추후 DB/설정으로 옮기기 쉽음
    private static final List<TutorialStepView> TUTORIAL_STEPS = List.of(
            // [수정] 포인트 영역 표시 박스(highlight*)·말풍선(bubbleText) 좌표 추가 + 그에 맞춰 누비 위치 재조정
            // 하이라이트/누비 좌표는 캡쳐본을 보고 대략 잡은 값 — 아래 숫자만 고치면 눈으로 보며 조정 가능
            TutorialStepView.of(1, "로드맵 기반 학습", "학습 순서를 따라가며 목표를 하나씩 달성하고 실력을 쌓아보세요",
                    "/assets/images/tutorial/1-learning-roadmap.png", 88, 30, "WAVING",
                    20, 21, 58, 71, "학습 진행도를 한 눈에 볼 수 있어요!"),
            TutorialStepView.of(2, "이론 → 문제풀이", "핵심 개념을 학습하고 바로 문제를 풀며 실력을 다져보세요",
                    "/assets/images/tutorial/2-learning-lessons.png", 65, 51, "READING",
                    82, 49, 16, 5, "개념을 천천히 쌓아가요."),
            TutorialStepView.of(3, "레벨 코딩 테스트", "학습한 내용을 바탕으로 실력을 점검하고 다음 단계에 도전하세요",
                    "/assets/images/tutorial/3-codingtest.png", 50, 15, "TELESCOPE",
                    1, 27, 96, 51, "AI가 만든 문제로 공부가 잘 됐는지 확인해봐요!"),
            TutorialStepView.of(4, "AI 학습 분석", "학습 기록과 시험 결과를 분석해 강점과 약점을 파악하고 맞춤 학습 방향을 제공합니다",
                    "/assets/images/tutorial/4-primeum.png", 25, 25, "IDEA",
                    2, 79, 95, 19, "코딩테스트 결과를 AI가 분석해줘요!"),
            TutorialStepView.of(5, "커뮤니티로 자유롭게 소통", "질문하고 답변하며 학습 경험을 공유하고 함께 성장하세요",
                    "/assets/images/tutorial/5-community.png", 11, 90, "PAINTING",
                    23, 20, 75, 74, "다른 사람들과 소통이 가능해요")
    );

    //서비스 루트 url
    @GetMapping("/")
    public String welcomeRoot(HttpSession session, Model model) {
        return resolveWelcomeView(session, model);
    }

    //웰컴, 튜토리얼 명시 route (alias)
    @GetMapping("/welcome")
    public String welcome(HttpSession session, Model model) {
        return resolveWelcomeView(session, model);
    }

    private String resolveWelcomeView(HttpSession session, Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("screen", "welcome/index");
        model.addAttribute("tutorialSteps", TUTORIAL_STEPS);   // [추가] 서버가 튜토리얼 단계 제공
        return "welcome/index";
    }

    private SessionUser currentUser(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        return value instanceof SessionUser u ? u : null;
    }
}