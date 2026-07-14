package com.acorn.elearning.learning.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.form.LevelTestForm;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.LevelTestService;
import com.acorn.elearning.learning.view.LevelTestQuestionView;
import com.acorn.elearning.learning.view.LevelTestResultView;
import com.acorn.elearning.learning.view.OnboardingProfileView;
import com.acorn.elearning.learning.view.OnboardingResultView;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import java.util.List;
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
 * 레벨 테스트(레벨 스캔) MVC 컨트롤러. 문항 표시(GET) → 제출(POST) → 결과(GET).
 * 화면은 learning/onboarding 템플릿을 온보딩과 공유하며 step=test / step=result 로 렌더한다.
 */
@Controller
public class LevelTestController {

    /** 과목 미지정 시 기본 과목(JAVA, subject_id=1). */
    private static final Long DEFAULT_SUBJECT_ID = 1L;

    /** 세션 미구현 구간에서 사용하는 fallback learner(샘플데이터 userId=2). */
    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private static final String ATTEMPT_STATUS_SUBMITTED = "SUBMITTED";

    private final LevelTestService levelTestService;
    private final LearningService learningService;

    public LevelTestController(LevelTestService levelTestService, LearningService learningService) {
        this.levelTestService = levelTestService;
        this.learningService = learningService;
    }

    /** 과목별 레벨 테스트 문항 표시(step=test). */
    @GetMapping("/learning/level-test")
    public String questions(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            Model model) {
        Long targetSubjectId = (subjectId != null) ? subjectId : DEFAULT_SUBJECT_ID;

        LevelTestForm form = new LevelTestForm();
        form.setSubjectId(targetSubjectId);

        List<LevelTestQuestionView> questions;
        try {
            questions = levelTestService.getQuestions(targetSubjectId);
        } catch (BusinessException e) {
            if (e.errorCode() != ErrorCode.COMMON_NOT_FOUND) {
                throw e;
            }
            questions = List.of();
        }

        model.addAttribute("step", "test");
        model.addAttribute("questions", questions);
        model.addAttribute("subjectId", targetSubjectId);
        model.addAttribute("levelTestForm", form);
        model.addAttribute("profile", levelTestProfile(sessionUser, targetSubjectId));
        return "learning/onboarding";
    }

    /** 답안 제출 → 채점/반영 후 결과로 redirect. */
    @PostMapping("/learning/level-test")
    public String submit(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("levelTestForm") LevelTestForm form,
            BindingResult bindingResult,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        if (bindingResult.hasErrors()) {
            model.addAttribute("step", "test");
            model.addAttribute("questions", levelTestService.getQuestions(form.getSubjectId()));
            model.addAttribute("subjectId", form.getSubjectId());
            model.addAttribute("profile", levelTestProfile(sessionUser, form.getSubjectId()));
            return "learning/onboarding";
        }

        LevelTestResultView result = levelTestService.submitAndApply(user, form);
        return "redirect:/learning/level-test/result/" + result.attemptId();
    }

    /** 결과 표시(step=result): 등급/정답수 + 출발 지점. */
    @GetMapping("/learning/level-test/result/{attemptId}")
    public String result(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long attemptId,
            Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        LevelTestResultView r = levelTestService.getResult(user, attemptId);
        model.addAttribute("step", "result");
        model.addAttribute("result", new OnboardingResultView(
                r.resultLevelCode(), r.correctCount(), r.totalCount(), true, startPlanetNo(r.resultLevelCode())));
        // 결과 화면의 이동 링크는 방금 응시한 과목으로 돌아가야 한다(주 과목이 아닌 과목도 응시할 수 있다).
        model.addAttribute("subjectId", r.subjectId());
        model.addAttribute("profile", levelTestProfile(sessionUser, r.subjectId()));
        return "learning/onboarding";
    }

    /** 화면 요약(과목명·목표). 세션 값이 아니라 응시 대상 과목으로 만든다. */
    private OnboardingProfileView levelTestProfile(SessionUser sessionUser, Long subjectId) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;
        return learningService.getLevelTestProfile(user, subjectId);
    }

    private static int startPlanetNo(String levelCode) {
        if ("GOLD".equals(levelCode)) {
            return 3;
        }
        if ("SILVER".equals(levelCode)) {
            return 2;
        }
        return 1;
    }
}
