package com.acorn.elearning.practice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PracticeController {

    @GetMapping("/learning/practice")
    public String index(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PracticeSetView view = practiceService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/practice");
        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets")
    public String createSet() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "learning/practice"; }
        // SessionUser sessionUser = currentSessionUser();
        // practiceService.createSet(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/practice";
    }

    @GetMapping("/learning/practice/problems/{problemId}")
    public String problemDetail(@PathVariable Long problemId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PracticeSetView view = practiceService.problemDetail(sessionUser, problemId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/practice");
        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/answers")
    public String submitAnswer(@PathVariable Long setAttemptId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "learning/practice"; }
        // SessionUser sessionUser = currentSessionUser();
        // problemService.submitAnswer(sessionUser, form, problemId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/practice";
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/complete")
    public String completeSet(@PathVariable Long setAttemptId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/learning"; }
        // SessionUser sessionUser = currentSessionUser();
        // practiceService.completeSet(sessionUser, form, setAttemptId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning";
    }
}
