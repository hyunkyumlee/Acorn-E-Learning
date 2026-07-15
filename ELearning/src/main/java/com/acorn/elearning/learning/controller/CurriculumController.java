package com.acorn.elearning.learning.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.learning.dto.response.LessonBookmarkPageResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkResponse;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.learning.service.LessonService;
import com.acorn.elearning.learning.view.LessonProgressView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 이론 학습 MVC 컨트롤러.
 * 라우트: 이론 상세(GET) / 이론 완료(POST) / 북마크(POST).
 */
@Controller
public class CurriculumController {

    private final CurriculumService curriculumService;
    private final LessonService lessonService;

    public CurriculumController(CurriculumService curriculumService, LessonService lessonService) {
        this.curriculumService = curriculumService;
        this.lessonService = lessonService;
    }

    @GetMapping("/learning/lessons/{lessonId}")
    public String lessonDetail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId, Model model) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;
        var lesson = curriculumService.getLessonDetail(lessonId);
        model.addAttribute("lesson", lesson);
        model.addAttribute("bookmarked", lessonService.isBookmarked(user, lessonId));
        // 이론 완료 상태: 완료된 레슨은 액션 버튼 대신 완료 표시(재클릭 시 409도 예방)
        model.addAttribute("lessonCompleted",
                lesson != null && curriculumService.isTheoryCompletedForLesson(user.userId(), lessonId));

        // 읽는 위치 맥락: 어느 행성(단원)의 몇 번째 레슨인지 + 다음 레슨
        if (lesson != null) {
            model.addAttribute("node", curriculumService.getNodeDetail(lesson.getNodeId()));
            var siblings = curriculumService.getLessonsByNode(lesson.getNodeId());
            model.addAttribute("lessonTotal", siblings.size());
            for (int i = 0; i < siblings.size(); i++) {
                if (lessonId.equals(siblings.get(i).getLessonId())) {
                    model.addAttribute("lessonIndex", i + 1);
                    if (i + 1 < siblings.size()) {
                        model.addAttribute("nextLessonId", siblings.get(i + 1).getLessonId());
                    }
                    break;
                }
            }
        }

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
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;

        // 화면 폼 제출이므로 실패 시에도 JSON(전역 핸들러) 대신 같은 상세로 안내 메시지와 함께 redirect.
        try {
            LessonProgressView result = lessonService.completeLesson(user, lessonId);
            redirectAttributes.addFlashAttribute("message",
                    "이론 학습을 완료했어요! (진행률 " + result.progressRate() + "%)");
            redirectAttributes.addFlashAttribute("nextAction", result.nextAction());
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/learning/lessons/" + lessonId;
    }

    @PostMapping("/learning/lessons/{lessonId}/bookmark")
    public String bookmark(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long lessonId,
            RedirectAttributes redirectAttributes) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;
        LessonBookmarkResponse result = lessonService.toggleBookmark(user, lessonId);
        redirectAttributes.addFlashAttribute("message",
                result.bookmarked() ? "북마크에 추가했어요." : "북마크를 해제했어요.");
        return "redirect:/learning/lessons/" + lessonId;
    }

    /** LEARN-007: 내가 북마크한 이론 목록(최신순 페이지). */
    @GetMapping("/learning/bookmarks")
    public String bookmarks(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            Model model) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        SessionUser user = sessionUser;
        LessonBookmarkPageResponse bookmarks = lessonService.getBookmarks(user, null, page, 20);
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("screen", "learning/bookmarks");
        return "learning/bookmarks";
    }
}
