package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/oauth/{provider}")
    public String redirect(@PathVariable String provider, HttpSession session) {
        return "redirect:" + oAuthService.startLoginRedirect(provider, session);
    }

    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider, @RequestParam String state, HttpSession session) {
        return "redirect:" + oAuthService.handleLoginCallback(provider, state, session);
    }

    @GetMapping("/settings/social/{provider}/connect")
    public String connectRedirect(@PathVariable String provider, @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser, HttpSession session) {
        return "redirect:" + oAuthService.startConnectRedirect(provider, sessionUser, session);
    }
    @GetMapping("/settings/social/{provider}/callback")
    public String connectCallback(@PathVariable String provider, @RequestParam String state, @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser, HttpSession session) {
        return "redirect:" + oAuthService.handleConnectCallback(provider, state, sessionUser, session);
    }


//    @GetMapping("/oauth/{provider}")
//    public String redirect(@PathVariable String provider, Model model) {
//        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
//        // SessionUser sessionUser = currentSessionUser();
//        // RedirectTarget redirectTarget = oAuthService.startLoginRedirect(provider, httpSession);
//        // return "redirect:" + redirectTarget.loginUrl();
//
//        model.addAttribute("screen", "redirect:/login");
//
//        return "redirect:/login";
//    }
//
//    @GetMapping("/oauth/{provider}/callback")
//    public String callback(@PathVariable String provider, Model model) {
//        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
//        // SessionUser sessionUser = currentSessionUser();
//        // RedirectTarget redirectTarget = oAuthService.handleLoginCallback(provider, requestParameters, httpSession);
//        // return "redirect:" + redirectTarget.afterLoginUrl();
//
//        model.addAttribute("screen", "redirect:/learning");
//
//        return "redirect:/learning";
//    }
//
//    @GetMapping("/settings/social/{provider}/connect")
//    public String connectRedirect(@PathVariable String provider, Model model) {
//        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
//        // SessionUser sessionUser = currentSessionUser();
//        // RedirectTarget redirectTarget = oAuthService.startConnectRedirect(provider, sessionUser, httpSession);
//        // return "redirect:" + redirectTarget.providerUrl();
//
//        model.addAttribute("screen", "redirect:/settings/social");
//
//        return "redirect:/settings/social";
//    }
//
//    @GetMapping("/settings/social/{provider}/callback")
//    public String connectCallback(@PathVariable String provider, Model model) {
//        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
//        // SessionUser sessionUser = currentSessionUser();
//        // RedirectTarget redirectTarget = oAuthService.handleConnectCallback(provider, requestParameters, sessionUser, httpSession);
//        // return "redirect:" + redirectTarget.settingsUrl();
//
//        model.addAttribute("screen", "redirect:/settings/social");
//
//        return "redirect:/settings/social";
//    }
}
