package com.acorn.elearning.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

//remember-me 영속쿠키: "userId.HMAC(userId)" 토큰. DB 없이 위조 방지 + 버전으로 무효화
// 기존 "userId.HMAC(userId)" -> 버전 포함 형식으로 변경
@Component
public class RememberMeCookie {
    private static final String COOKIE_NAME = "REMEMBER_ME";
    private static final int MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30일
    private final byte[] secret;

    public RememberMeCookie(@Value("${knowva.remember-me.secret}") String secret) {   // 기본값 없음 → 미설정 시 앱 기동 실패(fail-fast)
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    //로그인 성공 시 영속 쿠키 발급 (rememberMe=true)
    //version 인자 추가 - 발급 시점의 계정 버전을 토큰에 삼음
    public void issue(HttpServletResponse response, Long userId, long version) {
        String token = userId + "." + version + "." + sign(userId + ":" + version);
        Cookie cookie = new Cookie(COOKIE_NAME, token);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SECONDS);
        //cookie.setSecure(true); // 배포(https) 에서 활성화
        response.addCookie(cookie);
    }

    //세션 비었을 때 쿠키 검증 . userId 반환 (위조/없음 이면 null)
    //Long -> Token 변환
    //서명 위조 검증은 여기서, 버전 대조는 호출부(AuthService) 에서
    public Token resolve(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (!COOKIE_NAME.equals(c.getName())) continue;
            String[] parts = c.getValue().split("\\.");
            if (parts.length != 3) return null; // userId.version.sig 3조각
            if (!sign(parts[0] + ":" + parts[1]).equals(parts[2])) return null; //서명 불일치 = 위조

            try {
                return new Token(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
            } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    //로그아웃 시 쿠키 제거
    public void clear(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    //쿠키에서 뽑은 신원 + 버전
    public record Token(Long userId, Long version) { }
}
