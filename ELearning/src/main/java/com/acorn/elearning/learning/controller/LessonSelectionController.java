package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 레슨 선택 화면 MVC 컨트롤러.
 * 로드맵 행성 클릭(/learning/nodes/{nodeId}/lessons) → 그 행성의 레슨 목록 + 레슨별 진행상태.
 */
@Controller
public class LessonSelectionController {

    private static final SessionUser DEV_FALLBACK_USER =
            new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false);

    private final CurriculumService curriculumService;

    public LessonSelectionController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @GetMapping("/learning/nodes/{nodeId}/lessons")
    public String lessonList(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long nodeId, Model model) {
        SessionUser user = (sessionUser != null) ? sessionUser : DEV_FALLBACK_USER;
        model.addAttribute("node", curriculumService.getNodeDetail(nodeId));
        model.addAttribute("lessons", curriculumService.getLessonsByNode(nodeId));
        model.addAttribute("userLessonProgressMap",
                curriculumService.getLessonProgressMap(user.userId(), nodeId));
        model.addAttribute("completedRequiredLessonCount",
                curriculumService.countCompletedRequiredLessons(user.userId(), nodeId));
        model.addAttribute("totalRequiredLessonCount",
                curriculumService.countRequiredLessons(nodeId));
        model.addAttribute("screen", "learning/lesson-list");
        return "learning/lesson-list";
    }
}
