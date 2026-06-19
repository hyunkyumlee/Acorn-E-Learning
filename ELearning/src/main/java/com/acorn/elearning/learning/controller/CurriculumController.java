package com.acorn.elearning.learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CurriculumController {

    @GetMapping("/learning/lessons/{lessonId}")
    public String lessonDetail(@PathVariable Long lessonId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LessonDetailView view = curriculumService.lessonDetail(sessionUser, lessonId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
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
