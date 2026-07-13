package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
public class OAuthController {

    private final OAuthService oAuthService;
    private final SubjectMapper subjectMapper;   // [추가] 소셜 회원가입 화면 관심 과목 목록

    public OAuthController(OAuthService oAuthService, SubjectMapper subjectMapper) {   // [수정] subjectMapper 주입
        this.oAuthService = oAuthService;
        this.subjectMapper = subjectMapper;
    }

    // AUTH-005: provider 인가 페이지로 redirect
    @GetMapping("/oauth/{provider}")
    public String redirect(@PathVariable String provider, HttpSession session) {
        return "redirect:" + oAuthService.startLoginRedirect(provider, session);
    }

    // AUTH-006: 연동 계정 있으면 홈, 없으면 handleLoginCallback이 "/oauth/signup" 반환
    @GetMapping("/oauth/{provider}/callback")
    public String callback(@PathVariable String provider,
                           @RequestParam String code,
                           @RequestParam String state,
                           HttpSession session) {
        return "redirect:" + oAuthService.handleLoginCallback(provider, code, state, session);
    }

    // [추가] 소셜 회원가입 화면 — signup.html 재사용
    @GetMapping("/oauth/signup")
    public String socialSignupForm(HttpSession session, Model model) {
        String email = oAuthService.pendingEmail(session);
        if (email == null) {
            return "redirect:/login?social_expired=1";   // 세션 만료/직접 접근 방지
        }
        model.addAttribute("social", true);                                     // signup.html 소셜 분기 플래그
        model.addAttribute("socialEmail", email);
        model.addAttribute("suggestedNickname", oAuthService.pendingSuggestedNickname(session));
        model.addAttribute("screen", "auth/signup");
        addSubjectOptions(model);
        return "auth/signup";
    }

    // [추가] 소셜 회원가입 제출 → 폼 클래스 없이 @RequestParam으로 수신
    @PostMapping("/oauth/signup")
    public String socialSignupSubmit(HttpSession session,
                                     @RequestParam String nickname,
                                     @RequestParam(required = false) Long primarySubjectId,
                                     @RequestParam(required = false) String learningGoal,
                                     Model model) {
        if (oAuthService.pendingEmail(session) == null) {
            return "redirect:/login?social_expired=1";
        }
        // 닉네임 수동 검증 (폼 클래스가 없으므로 컨트롤러에서 처리)
        String trimmed = nickname == null ? "" : nickname.trim();
        if (trimmed.length() < 2 || trimmed.length() > 50) {
            return renderSocialSignup(model, session, trimmed, "닉네임은 2~50자로 입력해 주세요.");
        }
        try {
            String redirect = oAuthService.completeSocialSignup(session, trimmed, primarySubjectId, learningGoal);
            return "redirect:" + redirect;
        } catch (BusinessException ex) {   // 닉네임 중복·세션 만료 등 실패를 화면에 표시
            return renderSocialSignup(model, session, trimmed, ex.getMessage());
        }
    }

    // AUTH-009: 설정 화면 소셜 연결 시작
    @GetMapping("/settings/social/{provider}/connect")
    public String connectRedirect(@PathVariable String provider,
                                  @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser,
                                  HttpSession session) {
        return "redirect:" + oAuthService.startConnectRedirect(provider, sessionUser, session);
    }

    // AUTH-009: 설정 화면 소셜 연결 callback
    @GetMapping("/settings/social/{provider}/callback")
    public String connectCallback(@PathVariable String provider,
                                  @RequestParam String code,
                                  @RequestParam String state,
                                  @SessionAttribute(name = SessionUser.SESSION_KEY) SessionUser sessionUser,
                                  HttpSession session) {
        return "redirect:" + oAuthService.handleConnectCallback(provider, code, state, sessionUser, session);
    }

    // [추가] 소셜 회원가입 화면 재렌더 (입력한 닉네임·에러 유지)
    private String renderSocialSignup(Model model, HttpSession session, String nickname, String errorMessage) {
        model.addAttribute("social", true);
        model.addAttribute("socialEmail", oAuthService.pendingEmail(session));
        model.addAttribute("suggestedNickname", nickname);   // 방금 입력한 값 유지
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("screen", "auth/signup");
        addSubjectOptions(model);
        return "auth/signup";
    }

    // [추가] 관심 과목 dropdown용 활성 과목 목록 (이메일 회원가입과 동일 방식)
    private void addSubjectOptions(Model model) {
        List<Subject> subjects = subjectMapper.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .toList();
        model.addAttribute("subjects", subjects);
    }
}