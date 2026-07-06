package com.acorn.elearning.auth.service;

import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.auth.dto.response.GuestSessionResponse;
import com.acorn.elearning.auth.dto.response.UserSessionResponse;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    public void saveUser(HttpSession session, SessionUser sessionUser) {
        session.setAttribute(SessionUser.SESSION_KEY, sessionUser);
    }

    public Optional<SessionUser> getUser(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        if (value instanceof SessionUser sessionUser && sessionUser.userId() != null) {
            return Optional.of(sessionUser);
        }
        return Optional.empty();
    }

    public SessionUser getRequiredUser(HttpSession session) {
        return getUser(session).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SessionUser.SESSION_KEY);
    }

    public UserSessionResponse toLoginResponse(SessionUser sessionUser) {
        return UserSessionResponse.fromLogin(sessionUser);
    }

    public UserSessionResponse toLoginResponse(SessionUser sessionUser, String redirectUrl) {
        return UserSessionResponse.fromLogin(sessionUser, redirectUrl);
    }

    public GuestSessionResponse toGuestSessionResponse() {
        return GuestSessionResponse.guest();
    }

    //signup 응답 변환
    //AUTH-001 REST : email 포함 응답
    public UserSessionResponse toSignupResponse(SessionUser sessionUser) {
        return UserSessionResponse.fromSignup(sessionUser);
    }



//    public Map<String, Object> stub(String action) {
//        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
//        // SessionUser sessionUser = (SessionUser) httpSession.getAttribute("LOGIN_USER");
//        // if (sessionUser == null) { return Map.of("authenticated", false); }
//        // return Map.of("authenticated", true, "user", sessionUser);
//        return Map.of("action", action, "status", "SKELETON");
//    }
}
