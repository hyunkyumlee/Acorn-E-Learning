package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.mapper.LearningProfileWriteMapper;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.service.EnrollmentService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.LevelTestService;
import com.acorn.elearning.learning.view.OnboardingProfileView;
import com.acorn.elearning.learning.view.OnboardingResultView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 온보딩 위저드(과목 → 목표 → 출발 방식) MVC 컨트롤러.
 * 화면은 learning/onboarding 템플릿을 레벨 테스트와 공유하며, model의 step 값으로 섹션을 렌더한다.
 * 진행 중 선택값은 세션에 임시 보관하고, 마지막에 확정한다(PRG 패턴).
 */
@Controller
public class OnboardingController {

    public static final String SESSION_SUBJECT_ID = "ONB_SUBJECT_ID";
    public static final String SESSION_GOAL = "ONB_GOAL";

    /** 레벨 테스트 없이 시작할 때 부여하는 기본 레벨. */
    private static final String DEFAULT_LEVEL_CODE = "BRONZE";

    private final LearningService learningService;
    private final LearningProfileWriteMapper profileWriteMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;
    private final EnrollmentService enrollmentService;
    private final LevelTestService levelTestService;

    public OnboardingController(LearningService learningService,
                               LearningProfileWriteMapper profileWriteMapper,
                               UserLearningProfileMapper userLearningProfileMapper,
                               EnrollmentService enrollmentService,
                               LevelTestService levelTestService) {
        this.learningService = learningService;
        this.profileWriteMapper = profileWriteMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.enrollmentService = enrollmentService;
        this.levelTestService = levelTestService;
    }

    @GetMapping("/learning/onboarding")
    public String form(
            @RequestParam(name = "step", required = false, defaultValue = "intro") String step,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            HttpSession session, Model model) {
        model.addAttribute("step", step);
        if ("subject".equals(step)) {
            model.addAttribute("subjects", learningService.getActiveSubjects());
        }
        model.addAttribute("profile", buildProfile(session, sessionUser, learningService));
        return "learning/onboarding";
    }

    @PostMapping("/learning/onboarding/subject")
    public String subject(@RequestParam Long subjectId, HttpSession session) {
        session.setAttribute(SESSION_SUBJECT_ID, subjectId);
        return "redirect:/learning/onboarding?step=goal";
    }

    @PostMapping("/learning/onboarding/goal")
    public String goal(@RequestParam String learningGoal, HttpSession session) {
        session.setAttribute(SESSION_GOAL, learningGoal);
        return "redirect:/learning/onboarding?step=startmode";
    }

    @PostMapping("/learning/onboarding/start")
    public String start(
            @RequestParam String startMode,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            HttpSession session, Model model) {
        Long subjectId = (Long) session.getAttribute(SESSION_SUBJECT_ID);
        if (subjectId == null) {
            // 세션이 끊겨 과목 선택이 비면 수강 신청을 확정할 수 없다(subject_id는 NOT NULL).
            return "redirect:/learning/onboarding?step=subject";
        }
        String learningGoal = (String) session.getAttribute(SESSION_GOAL);
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;
        // 온보딩에서 고른 과목/목표를 프로필에 확정(BASIC·SCAN 공통).
        userLearningProfileMapper.updateOnboarding(user.userId(), subjectId, learningGoal);
        // 문항이 등록되지 않은 과목은 레벨 테스트로 시작할 수 없어 기초 시작으로 처리한다.
        if ("SCAN".equals(startMode) && levelTestService.hasQuestions(subjectId)) {
            // 레벨 테스트로 시작: 신청만 하고 레벨은 열지 않는다(채점 결과가 판정 등급까지 연다).
            enrollmentService.enroll(user.userId(), subjectId, EnrollmentService.START_MODE_LEVEL_TEST);
            return "redirect:/learning/level-test?subjectId=" + subjectId;
        }
        // 기초부터 시작: 신청과 함께 최저 레벨을 연다.
        enrollmentService.enroll(user.userId(), subjectId, EnrollmentService.START_MODE_BASIC);
        profileWriteMapper.updateLevel(user.userId(), DEFAULT_LEVEL_CODE);
        model.addAttribute("step", "result");
        model.addAttribute("result", new OnboardingResultView(DEFAULT_LEVEL_CODE, 0, 0, false, 1));
        model.addAttribute("profile", buildProfile(session, sessionUser, learningService));
        return "learning/onboarding";
    }

    /** 세션에 담긴 선택값으로 화면 요약 profile을 만든다. */
    static OnboardingProfileView buildProfile(HttpSession session, SessionUser sessionUser, LearningService learningService) {
        Long subjectId = (Long) session.getAttribute(SESSION_SUBJECT_ID);
        String learningGoal = (String) session.getAttribute(SESSION_GOAL);
        String nickname = (sessionUser != null) ? sessionUser.nickname() : null;
        String subjectName = null;
        if (subjectId != null) {
            for (Subject s : learningService.getActiveSubjects()) {
                if (subjectId.equals(s.getSubjectId())) {
                    subjectName = s.getSubjectName();
                    break;
                }
            }
        }
        return new OnboardingProfileView(nickname, subjectId, subjectName, learningGoal);
    }
}
