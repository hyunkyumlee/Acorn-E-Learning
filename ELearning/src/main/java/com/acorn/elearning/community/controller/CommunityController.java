package com.acorn.elearning.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunityController {

    @GetMapping("/community")
    public String home(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostPageView view = communityService.home(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "community/index");
        return "community/index";
    }
}
