package com.acorn.elearning.learning.controller;

import com.acorn.elearning.learning.service.CurriculumService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @GetMapping("/learning/lessons/{lessonId}")
    public String lessonDetail(@PathVariable Long lessonId, Model model) {
        // TODO 추후 signature에 HttpSession/SessionUser를 추가하고 진행률/북마크도 함께 조회한다.
        // 현재는 lessons 테이블에서 단일 lesson 상세를 조회해 표시한다.
        model.addAttribute("lesson", curriculumService.getLessonDetail(lessonId));
        model.addAttribute("screen", "learning/curriculum");
        return "learning/curriculum";
    }

    @PostMapping("/learning/lessons/{lessonId}/complete")
    public String completeLesson(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "learning/curriculum"; }
        // SessionUser sessionUser = currentSessionUser();
        // lessonService.completeLesson(sessionUser, form, lessonId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/lessons/{lessonId}";
    }

    @PostMapping("/learning/lessons/{lessonId}/bookmark")
    public String bookmark(@PathVariable Long lessonId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/learning/lessons/{lessonId}"; }
        // SessionUser sessionUser = currentSessionUser();
        // lessonService.bookmark(sessionUser, form, lessonId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/lessons/{lessonId}";
    }
}
