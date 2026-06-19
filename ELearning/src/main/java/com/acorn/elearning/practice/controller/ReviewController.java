package com.acorn.elearning.practice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReviewController {

    @GetMapping("/learning/review")
    public String summary(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WrongAnswerSummaryView view = wrongAnswerService.summary(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/review");
        return "learning/review";
    }

    @GetMapping("/learning/review/list")
    public String list(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WrongAnswerPageView view = wrongAnswerService.list(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/review-list");
        return "learning/review-list";
    }

    @GetMapping("/learning/review/{wrongAnswerId}")
    public String detail(@PathVariable Long wrongAnswerId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WrongAnswerSummaryView view = wrongAnswerService.detail(sessionUser, wrongAnswerId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/review");
        return "learning/review";
    }

    @PostMapping("/learning/review/{wrongAnswerId}/retry")
    public String retry(@PathVariable Long wrongAnswerId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "learning/review"; }
        // SessionUser sessionUser = currentSessionUser();
        // wrongAnswerService.retry(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/review";
    }

    @PostMapping("/learning/review/{wrongAnswerId}/reviewed")
    public String markReviewed(@PathVariable Long wrongAnswerId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/learning/review"; }
        // SessionUser sessionUser = currentSessionUser();
        // wrongAnswerService.markReviewed(sessionUser, form, wrongAnswerId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/review";
    }
}
