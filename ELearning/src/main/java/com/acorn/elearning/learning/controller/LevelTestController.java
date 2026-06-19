package com.acorn.elearning.learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LevelTestController {

    @GetMapping("/learning/level-test")
    public String questions(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // OnboardingView view = levelTestService.questions(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/onboarding");
        return "learning/onboarding";
    }

    @PostMapping("/learning/level-test")
    public String submit() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "learning/onboarding"; }
        // SessionUser sessionUser = currentSessionUser();
        // levelTestService.submit(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/level-test/result/1";
    }

    @GetMapping("/learning/level-test/result/{attemptId}")
    public String result(@PathVariable Long attemptId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // OnboardingView view = levelTestService.result(sessionUser, attemptId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "learning/onboarding");
        return "learning/onboarding";
    }
}
