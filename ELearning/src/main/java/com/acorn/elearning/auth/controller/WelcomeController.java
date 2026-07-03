package com.acorn.elearning.auth.controller;

import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String welcomeRoot(HttpSession session, Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("screen", "welcome/index");
        return "welcome/index";
    }

    @GetMapping("/welcome")
    public String welcome(HttpSession session, Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("screen", "welcome/index");
        return "welcome/index";
    }

    private SessionUser currentUser(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        return value instanceof SessionUser u ? u : null;
    }
//    @GetMapping("/")
//    public String welcomeRoot(Model model) {
//        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
//        // SessionUser sessionUser = currentSessionUser();
//        // AuthPageView view = authPageService.welcomeRoot(sessionUser);
//        // model.addAttribute("view", view);
//        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
//        model.addAttribute("screen", "welcome/index");
//        return "welcome/index";
//    }
//
//    @GetMapping("/welcome")
//    public String welcome(Model model) {
//        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
//        // SessionUser sessionUser = currentSessionUser();
//        // AuthPageView view = authPageService.welcome(sessionUser);
//        // model.addAttribute("view", view);
//        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
//        model.addAttribute("screen", "welcome/index");
//        return "welcome/index";
//    }
}
