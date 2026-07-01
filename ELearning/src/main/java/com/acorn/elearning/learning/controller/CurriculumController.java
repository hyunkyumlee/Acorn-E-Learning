package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LessonService;
import com.acorn.elearning.learning.view.LessonProgressView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 이론 학습(SR-005) MVC 컨트롤러.
 * 라우트: 이론 상세(GET) / 이론 완료(POST) / 북마크(POST).
 * 현재 브랜치 범위 = completeLesson(LEARN-005) 배선. lessonDetail은 기존 단순 조회, bookmark는 후속 브랜치.
 */
@Controller
public class CurriculumController {

    /**
     * 개발용 fallback 사용자: 로그인/세션은 1번(auth) 담당이라 구현 전까지 세션이 비어 있다.
     * 세션이 없으면 샘플데이터의 learner(userId=2, 누비학습자)로 확인한다.
     * (LearningController/LevelTestController와 동일 패턴 — 로그인/세션이 붙으면 자연히 미사용)
     */
    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private final CurriculumService curriculumService;
    private final LessonService lessonService;

    public CurriculumController(CurriculumService curriculumService, LessonService lessonService) {
        this.curriculumService = curriculumService;
        this.lessonService = lessonService;
    }

    @GetMapping("/learning/lessons/{lessonId}")
    public String lessonDetail(@PathVariable Long lessonId, Model model) {
        // TODO(후속 curriculum-read 브랜치) SessionUser로 진행률/북마크 상태도 함께 조회한다.
        // 현재는 lessons 테이블에서 단일 lesson 상세를 조회해 표시한다.
        model.addAttribute("lesson", curriculumService.getLessonDetail(lessonId));
        model.addAttribute("screen", "learning/curriculum");
        return "learning/curriculum";
    }

    /**
     * LEARN-005: 이론 완료 처리. learning_progress upsert(출석 X) 후 같은 상세로 redirect + flash.
     * (MVC 명세 v1.4: redirect / "next lesson 또는 same detail")
     */
    @PostMapping("/learning/lessons/{lessonId}/complete")
    public String completeLesson(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId,
            RedirectAttributes redirectAttributes) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;

        LessonProgressView result = lessonService.completeLesson(user, lessonId);
        redirectAttributes.addFlashAttribute("message",
                "이론 학습을 완료했어요! (진행률 " + result.progressRate() + "%)");
        redirectAttributes.addFlashAttribute("nextAction", result.nextAction());
        return "redirect:/learning/lessons/" + lessonId;
    }

    @PostMapping("/learning/lessons/{lessonId}/bookmark")
    public String bookmark(@PathVariable Long lessonId) {
        // TODO(후속 lesson-bookmark 브랜치) LessonService.bookmark(sessionUser, lessonId) 구현.
        return "redirect:/learning/lessons/" + lessonId;
    }
}
