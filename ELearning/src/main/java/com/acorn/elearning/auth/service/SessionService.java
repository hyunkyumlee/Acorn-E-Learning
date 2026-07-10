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
        // removeAttribute 만 하면 세션 ID가 유지돼 세션 고정(session fixation) 공격에 취약 -> 세션 자체를 폐기
        session.invalidate();
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
}
