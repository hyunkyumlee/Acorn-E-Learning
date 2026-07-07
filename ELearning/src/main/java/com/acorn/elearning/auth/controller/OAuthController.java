package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
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

    // AUTH-005 (변경 없음): provider 인가 페이지로 redirect
    @GetMapping("/oauth/{provider}")
    public String redirect(@PathVariable String provider, HttpSession session) {
        return "redirect:" + oAuthService.startLoginRedirect(provider, session);
    }

    // AUTH-006 [수정]: provider가 전달하는 인가 code 파라미터 추가
    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider,
                           @RequestParam String code,     // [추가] 인가 코드
                           @RequestParam String state,
                           HttpSession session) {
        return "redirect:" + oAuthService.handleLoginCallback(provider, code, state, session);
    }

    // AUTH-009 (변경 없음): 설정 화면 소셜 연결 시작
    @GetMapping("/settings/social/{provider}/connect")
    public String connectRedirect(@PathVariable String provider,
                                  @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser,
                                  HttpSession session) {
        return "redirect:" + oAuthService.startConnectRedirect(provider, sessionUser, session);
    }

    // AUTH-009 [수정]: 설정 화면 소셜 연결 callback — code 파라미터 추가
    @GetMapping("/settings/social/{provider}/callback")
    public String connectCallback(@PathVariable String provider,
                                  @RequestParam String code,   // [추가] 인가 코드
                                  @RequestParam String state,
                                  @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser,
                                  HttpSession session) {
        return "redirect:" + oAuthService.handleConnectCallback(provider, code, state, sessionUser, session);
    }

    // [삭제] 하단 주석 처리된 SKELETON 예시 메소드 4개 전체 제거
}