package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.form.LevelTestForm;
import com.acorn.elearning.learning.service.LevelTestService;
import com.acorn.elearning.learning.view.LevelTestResultView;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 레벨 테스트(SR-004, LEVEL-001/002) MVC 컨트롤러.
 * 문항 표시(GET) → 제출(POST) → 결과(GET) 세 라우트. 화면은 learning/onboarding 템플릿을 공유한다.
 */
@Controller
public class LevelTestController {

    /** 과목 미지정 시 기본 과목: JAVA(subject_id=1). 온보딩의 과목 선택이 붙기 전 dev/스모크용. */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    /**
     * 개발용 fallback 사용자: 로그인/세션은 1번(auth) 담당이라 구현 전까지 세션이 비어 있다.
     * 세션이 없으면 샘플데이터의 learner(userId=2, 누비학습자)로 확인한다.
     * (LearningController와 동일 패턴 — 로그인/세션이 붙으면 자연히 미사용)
     */
    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private final LevelTestService levelTestService;

    public LevelTestController(LevelTestService levelTestService) {
        this.levelTestService = levelTestService;
    }

    /** LEVEL-001: 과목별 레벨 테스트 문항 표시. */
    @GetMapping("/learning/level-test")
    public String questions(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            Model model) {
        Long targetSubjectId = (subjectId != null) ? subjectId : DEFAULT_SUBJECT_ID;

        LevelTestForm form = new LevelTestForm();
        form.setSubjectId(targetSubjectId);

        model.addAttribute("questions", levelTestService.getQuestions(targetSubjectId));
        model.addAttribute("subjectId", targetSubjectId);
        model.addAttribute("levelTestForm", form);
        model.addAttribute("screen", "learning/onboarding");
        return "learning/onboarding";
    }

    /** LEVEL-002: 답안 제출 → 채점/반영 후 결과 화면으로 redirect. */
    @PostMapping("/learning/level-test")
    public String submit(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("levelTestForm") LevelTestForm form,
            BindingResult bindingResult,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        if (bindingResult.hasErrors()) {
            // 과목 등 필수값 오류 시 문항을 다시 실어 문항 화면을 재표시한다.
            model.addAttribute("questions", levelTestService.getQuestions(form.getSubjectId()));
            model.addAttribute("subjectId", form.getSubjectId());
            model.addAttribute("screen", "learning/onboarding");
            return "learning/onboarding";
        }

        LevelTestResultView result = levelTestService.submitAndApply(user, form);
        return "redirect:/learning/level-test/result/" + result.attemptId();
    }

    /** LEVEL-002 결과: 등급/정답수 표시. */
    @GetMapping("/learning/level-test/result/{attemptId}")
    public String result(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long attemptId,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        model.addAttribute("result", levelTestService.getResult(user, attemptId));
        model.addAttribute("screen", "learning/onboarding");
        return "learning/onboarding";
    }
}
