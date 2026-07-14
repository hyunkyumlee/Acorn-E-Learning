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
            TutorialStepView.of(1, "로드맵 기반 학습", "학습 순서를 따라가며 목표를 하나씩 달성하고 실력을 쌓아보세요",
                    null, 62, 30, "EXPLORING"),
            TutorialStepView.of(2, "이론 → 문제풀이", "핵심 개념을 학습하고 바로 문제를 풀며 실력을 다져보세요",
                    null, 20, 70, "STUDYING"),
            TutorialStepView.of(3, "레벨 코딩 테스트", "학습한 내용을 바탕으로 실력을 점검하고 다음 단계에 도전하세요",
                    null, 75, 65, "THINKING"),
            TutorialStepView.of(4, "AI 학습 분석", "학습 기록과 시험 결과를 분석해 강점과 약점을 파악하고 맞춤 학습 방향을 제공합니다",
                    null, 25, 25, "THINKING"),
            TutorialStepView.of(5, "커뮤니티로 자유롭게 소통", "질문하고 답변하며 학습 경험을 공유하고 함께 성장하세요",
                    null, 55, 45, "GREETING")
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