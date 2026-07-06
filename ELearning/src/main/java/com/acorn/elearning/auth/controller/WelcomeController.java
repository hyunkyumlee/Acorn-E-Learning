package com.acorn.elearning.auth.controller;

import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    //서비스 루트 url
    //브라우저가 도메인만 입력했을 때 GUEST 는 welcome, 로그인 사용자는 role 별 기본 화면으로 보냄
    @GetMapping("/")
    public String welcomeRoot(HttpSession session, Model model) {
        return resolveWelcomeView(session, model);
    }

    //웰컴, 튜토리얼 명시 route
    // `/` 와 동일한 view 를 반환하며, 북마크, 외부 링크 진입용 alias 이다.
    @GetMapping("/welcome")
    public String welcome(HttpSession session, Model model) {
        return resolveWelcomeView(session, model);
    }

    //GUEST / 로그인 사용자 분기 공통 처리
    // 1) 세션에 LOGIN_USER 가 있으면 role 별로 redirect
    // 2) 없으면 welcome/index template 렌더 (DB 조회 없음)
    private String resolveWelcomeView(HttpSession session, Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("screen", "welcome/index");
        return "welcome/index";
    }

    //LOGIN_USER 에서 SessionUser 가 없거나 타입이 다르면 null(GUEST) 로 취급
    private SessionUser currentUser(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        return value instanceof SessionUser u ? u : null;
    }
}
