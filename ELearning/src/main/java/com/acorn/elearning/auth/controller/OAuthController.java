package com.acorn.elearning.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OAuthController {

    @GetMapping("/oauth/{provider}")
    public String redirect(@PathVariable String provider, Model model) {
        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
        // SessionUser sessionUser = currentSessionUser();
        // RedirectTarget redirectTarget = oAuthService.startLoginRedirect(provider, httpSession);
        // return "redirect:" + redirectTarget.loginUrl();

        model.addAttribute("screen", "redirect:/login");

        return "redirect:/login";
    }

    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider, Model model) {
        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
        // SessionUser sessionUser = currentSessionUser();
        // RedirectTarget redirectTarget = oAuthService.handleLoginCallback(provider, requestParameters, httpSession);
        // return "redirect:" + redirectTarget.afterLoginUrl();

        model.addAttribute("screen", "redirect:/learning");

        return "redirect:/learning";
    }

    @GetMapping("/settings/social/{provider}/connect")
    public String connectRedirect(@PathVariable String provider, Model model) {
        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
        // SessionUser sessionUser = currentSessionUser();
        // RedirectTarget redirectTarget = oAuthService.startConnectRedirect(provider, sessionUser, httpSession);
        // return "redirect:" + redirectTarget.providerUrl();

        model.addAttribute("screen", "redirect:/settings/social");

        return "redirect:/settings/social";
    }

    @GetMapping("/settings/social/{provider}/callback")
    public String connectCallback(@PathVariable String provider, Model model) {
        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
        // SessionUser sessionUser = currentSessionUser();
        // RedirectTarget redirectTarget = oAuthService.handleConnectCallback(provider, requestParameters, sessionUser, httpSession);
        // return "redirect:" + redirectTarget.settingsUrl();

        model.addAttribute("screen", "redirect:/settings/social");

        return "redirect:/settings/social";
    }
}
