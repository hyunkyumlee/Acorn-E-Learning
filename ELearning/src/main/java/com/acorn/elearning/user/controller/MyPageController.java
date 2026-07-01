package com.acorn.elearning.user.controller;

import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.MyPageSummaryResponse;
import com.acorn.elearning.user.service.UserActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class MyPageController {
    private final UserActivityService userActivityService;

    public MyPageController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/mypage")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        MyPageSummaryResponse view = userActivityService.mypage(sessionUser);
        model.addAttribute("screen", "mypage/index");
        model.addAttribute("view", view);
        return "mypage/index";
    }
}
