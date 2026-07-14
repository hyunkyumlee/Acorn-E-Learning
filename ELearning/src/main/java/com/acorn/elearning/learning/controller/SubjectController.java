package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.EnrollmentService;
import com.acorn.elearning.learning.service.LearningService;
import com.acorn.elearning.learning.service.LevelTestService;
import com.acorn.elearning.learning.service.ProgressService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 과목 소개 화면 MVC 컨트롤러.
 * 수강하지 않은 과목은 로드맵 대신 이 화면으로 들어오고, 여기서 수강신청을 한다.
 */
@Controller
public class SubjectController {

    private final LearningService learningService;
    private final CurriculumService curriculumService;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;
    private final LevelTestService levelTestService;

    public SubjectController(LearningService learningService,
                             CurriculumService curriculumService,
                             EnrollmentService enrollmentService,
                             ProgressService progressService,
                             LevelTestService levelTestService) {
        this.learningService = learningService;
        this.curriculumService = curriculumService;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
        this.levelTestService = levelTestService;
    }

    @GetMapping("/learning/subjects/{subjectId}")
    public String subjectDetail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long subjectId, Model model) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;

        model.addAttribute("subject", learningService.getSubject(subjectId));
        model.addAttribute("enrolled", enrollmentService.isEnrolled(user.userId(), subjectId));
        model.addAttribute("levelSummaries", curriculumService.getLevelSummaries(subjectId));
        model.addAttribute("progressPercent",
                progressService.computeSubjectProgress(user.userId()).getOrDefault(subjectId, 0));
        // 레벨 테스트 문항이 아직 등록되지 않은 과목은 테스트로 시작할 수 없다(기초부터 시작만 가능).
        model.addAttribute("levelTestAvailable", levelTestService.hasQuestions(subjectId));

        model.addAttribute("screen", "learning/subject-detail");
        return "learning/subject-detail";
    }

    /**
     * 과목 수강신청. 기초부터 시작(BASIC)이면 최저 레벨이 바로 열려 로드맵으로 이동하고,
     * 레벨 테스트(LEVEL_TEST)면 레벨을 열지 않은 채 테스트 화면으로 이동한다.
     */
    @PostMapping("/learning/subjects/{subjectId}/enroll")
    public String enroll(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long subjectId,
            @RequestParam(name = "startMode", required = false) String startMode) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;

        learningService.getSubject(subjectId);

        boolean byLevelTest = EnrollmentService.START_MODE_LEVEL_TEST.equals(startMode)
                && levelTestService.hasQuestions(subjectId);
        String mode = byLevelTest
                ? EnrollmentService.START_MODE_LEVEL_TEST
                : EnrollmentService.START_MODE_BASIC;

        enrollmentService.enroll(user.userId(), subjectId, mode);

        return byLevelTest
                ? "redirect:/learning/level-test?subjectId=" + subjectId
                : "redirect:/learning?subjectId=" + subjectId;
    }
}
