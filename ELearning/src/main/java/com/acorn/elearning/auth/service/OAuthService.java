package com.acorn.elearning.auth.service;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import com.acorn.elearning.auth.dto.response.SocialAccountListResponse;
import com.acorn.elearning.auth.dto.response.SocialAccountResponse;
import com.acorn.elearning.auth.mapper.SocialAccountMapper;
import com.acorn.elearning.auth.model.SocialAccount;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.config.OAuthProperties;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.mapper.UserSettingMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import com.acorn.elearning.user.model.UserSetting;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class OAuthService {

    private static final String OAUTH_STATE_KEY = "OAUTH_STATE";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String PENDING_SOCIAL_KEY = "PENDING_SOCIAL_LINK";
    private static final String GITHUB_EMAILS_URI = "https://api.github.com/user/emails";   // [추가] GitHub 이메일 2차 조회

    private final SocialAccountMapper socialAccountMapper;
    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;
    private final SessionService sessionService;
    private final OAuthProperties oAuthProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final RestClient restClient = RestClient.create();

    public OAuthService(SocialAccountMapper socialAccountMapper,
                        UserMapper userMapper,
                        UserSettingMapper userSettingMapper,
                        UserLearningProfileMapper userLearningProfileMapper,
                        SessionService sessionService,
                        OAuthProperties oAuthProperties) {
        this.socialAccountMapper = socialAccountMapper;
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.sessionService = sessionService;
        this.oAuthProperties = oAuthProperties;
    }

    // ================= AUTH-005: 로그인 provider redirect =================
    public String startLoginRedirect(String provider, HttpSession session) {
        OAuthProperties.Provider cfg = oAuthProperties.require(provider);
        String state = issueState(session, provider);
        return buildAuthorizationUrl(cfg, state);
    }

    // ================= AUTH-006: 로그인 callback (식별 + 검증 연동) =================
    @Transactional
    public String handleLoginCallback(String provider, String code, String state, HttpSession session) {
        OAuthProperties.Provider cfg = oAuthProperties.require(provider);
        validateState(session, provider, state);
        session.removeAttribute(OAUTH_STATE_KEY);

        OAuthUserInfo info = fetchUserInfo(provider, cfg, code);   // token 교환 + userinfo (+ github 이메일)

        // (1) 식별 앵커 = provider + providerUserId. 이미 연동돼 있으면 그 회원으로 로그인
        SocialAccount linked = socialAccountMapper
                .findByProviderAndProviderUserId(provider, info.providerUserId()).orElse(null);
        if (linked != null && Boolean.TRUE.equals(linked.getIsActive())) {
            User user = userMapper.findById(linked.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND));
            SessionUser sessionUser = toSessionUser(user);
            sessionService.saveUser(session, sessionUser);
            return sessionUser.defaultRedirectPath();
        }

        // (3) 미연동 + provider가 "검증한" email이 기존 회원과 일치 → 비밀번호 로그인 유도(pending)
        //     ★ 계정 검증 핵심: emailVerified=true 일 때만 기존 계정 매칭 (미검증/없음이면 무시하고 신규 취급)
        User existing = (info.email() != null && info.emailVerified())
                ? userMapper.findByEmail(info.email()).orElse(null)
                : null;
        if (existing != null) {
            session.setAttribute(PENDING_SOCIAL_KEY,
                    new PendingSocialLink(provider, info.providerUserId(), info.email()));
            return "/login?linkPending=" + enc(provider) + "&email=" + enc(info.email());
        }

        // (2) 비회원 → 소셜 전용 회원가입 + 연동 후 로그인
        User created = registerSocialUser(provider, info);
        insertSocialAccount(created.getUserId(), provider, info);
        SessionUser sessionUser = toSessionUser(created);
        sessionService.saveUser(session, sessionUser);
        return sessionUser.defaultRedirectPath();
    }

    // [Part C] 비밀번호 로그인 성공 직후 호출 — 대기 중(pending) 소셜 연동이 있으면 자동 연동
    @Transactional
    public void consumePendingLink(HttpSession session, SessionUser sessionUser) {
        Object v = session.getAttribute(PENDING_SOCIAL_KEY);
        if (!(v instanceof PendingSocialLink pending)) {
            return;
        }
        session.removeAttribute(PENDING_SOCIAL_KEY);

        boolean already = socialAccountMapper
                .findByProviderAndProviderUserId(pending.provider(), pending.providerUserId())
                .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                .isPresent();
        if (already) {
            return;
        }
        // pending은 검증된 이메일일 때만 저장되므로 emailVerified=true로 취급
        OAuthUserInfo info = new OAuthUserInfo(pending.providerUserId(), pending.providerEmail(), null, true);
        insertSocialAccount(sessionUser.userId(), pending.provider(), info);
    }

    // ================= AUTH-009: 설정 화면 소셜 연결 redirect =================
    public String startConnectRedirect(String provider, SessionUser sessionUser, HttpSession session) {
        requireLogin(sessionUser);
        OAuthProperties.Provider cfg = oAuthProperties.require(provider);
        String state = issueState(session, provider);
        return buildAuthorizationUrl(cfg, state);
    }

    // ================= AUTH-009: 설정 화면 소셜 연결 callback =================
    @Transactional
    public String handleConnectCallback(String provider, String code, String state, SessionUser sessionUser, HttpSession session) {
        requireLogin(sessionUser);
        OAuthProperties.Provider cfg = oAuthProperties.require(provider);
        validateState(session, provider, state);
        session.removeAttribute(OAUTH_STATE_KEY);

        OAuthUserInfo info = fetchUserInfo(provider, cfg, code);
        socialAccountMapper.findByProviderAndProviderUserId(provider, info.providerUserId())
                .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                .ifPresent(a -> { throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 연결된 소셜 계정입니다."); });
        insertSocialAccount(sessionUser.userId(), provider, info);
        return "/settings/social";
    }

    // ================= AUTH-007: 연결된 소셜 계정 목록 =================
    public SocialAccountListResponse socialAccounts(SessionUser sessionUser) {
        requireLogin(sessionUser);
        List<SocialAccount> rows = socialAccountMapper.findByUserId(sessionUser.userId());
        return SocialAccountListResponse.from(rows);
    }

    // ================= AUTH-008: 소셜 계정 연결 해제 (soft delete) =================
    @Transactional
    public SocialAccountResponse deleteSocialAccount(SessionUser sessionUser, String provider) {
        requireLogin(sessionUser);
        SocialAccount account = socialAccountMapper.findByUserId(sessionUser.userId()).stream()
                .filter(a -> provider.equals(a.getProvider()) && Boolean.TRUE.equals(a.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "연결된 소셜 계정이 없습니다."));
        account.setIsActive(false);
        account.setDisconnectedAt(LocalDateTime.now());
        socialAccountMapper.update(account);
        return SocialAccountResponse.from(account);
    }

    // ------------------------- 내부 helper -------------------------

    // 소셜 전용 신규 회원 생성 (비밀번호 credential 없음)
    // [수정] provider 인자 추가 + email null 대비(대체 이메일 생성) — users.email NOT NULL 대응
    private User registerSocialUser(String provider, OAuthUserInfo info) {
        String email = (info.email() != null && !info.email().isBlank())
                ? info.email()
                : provider + "_" + info.providerUserId() + "@social.knowva.local";   // 이메일 미제공 대비

        User user = new User();
        user.setEmail(email);
        user.setNickname(resolveNickname(provider, info));
        user.setRole(SessionUser.ROLE_USER);
        user.setStatus(STATUS_ACTIVE);
        userMapper.insert(user);

        UserSetting setting = new UserSetting();
        setting.setUserId(user.getUserId());
        setting.setTheme("SYSTEM");
        setting.setNotificationEnabled(true);
        setting.setReducedMotionEnabled(false);
        userSettingMapper.insert(setting);

        UserLearningProfile profile = new UserLearningProfile();
        profile.setUserId(user.getUserId());
        profile.setCurrentLevelCode("BRONZE");
        profile.setTotalScore(0);
        userLearningProfileMapper.insert(profile);
        return user;
    }

    // social_accounts 신규 연동 row 생성
    private void insertSocialAccount(Long userId, String provider, OAuthUserInfo info) {
        SocialAccount account = new SocialAccount();
        account.setUserId(userId);
        account.setProvider(provider);
        account.setProviderUserId(info.providerUserId());
        account.setProviderEmail(info.email());
        account.setIsActive(true);
        socialAccountMapper.insert(account);
    }

    // provider에서 token 교환 후 userinfo 조회 (google/github 분기)
    // [수정] github 분기 추가 + Accept 헤더
    private OAuthUserInfo fetchUserInfo(String provider, OAuthProperties.Provider cfg, String code) {
        String accessToken = exchangeToken(cfg, code);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = restClient.get()
                .uri(cfg.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .body(Map.class);
        if (body == null) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "소셜 사용자 정보 조회 실패");
        }
        return "github".equals(provider) ? parseGithub(body, accessToken) : parseGoogle(body);
    }

    // authorization code → access token (form-urlencoded POST)
    // [수정] Accept: application/json 헤더 추가 — GitHub는 없으면 form 응답이라 JSON 파싱 실패
    private String exchangeToken(OAuthProperties.Provider cfg, String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", cfg.getClientId());
        form.add("client_secret", cfg.getClientSecret());
        form.add("redirect_uri", cfg.getRedirectUri());
        form.add("code", code);
        @SuppressWarnings("unchecked")
        Map<String, Object> token = restClient.post()
                .uri(cfg.getTokenUri())
                .header("Accept", "application/json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
        if (token == null || token.get("access_token") == null) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "소셜 토큰 발급 실패");
        }
        return String.valueOf(token.get("access_token"));
    }

    // Google userinfo: { sub, email, email_verified, name }
    // [수정] email_verified 반영
    private OAuthUserInfo parseGoogle(Map<String, Object> body) {
        boolean verified = Boolean.parseBoolean(String.valueOf(body.get("email_verified")));
        return new OAuthUserInfo(String.valueOf(body.get("sub")),
                (String) body.get("email"), (String) body.get("name"), verified);
    }

    // [추가] GitHub: /user( id, login, name, email ) + /user/emails( primary, verified )
    // GitHub는 공개 이메일만 /user에 담기므로, 검증여부·비공개 대비 /user/emails를 2차 호출해 primary 선택
    @SuppressWarnings("unchecked")
    private OAuthUserInfo parseGithub(Map<String, Object> user, String accessToken) {
        String id = String.valueOf(user.get("id"));
        String name = (String) user.get("name");
        String login = (String) user.get("login");
        String email = (String) user.get("email");
        boolean verified = false;

        List<Map<String, Object>> emails = restClient.get()
                .uri(GITHUB_EMAILS_URI)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(List.class);
        if (emails != null) {
            for (Map<String, Object> e : emails) {
                if (Boolean.TRUE.equals(e.get("primary"))) {
                    email = (String) e.get("email");
                    verified = Boolean.TRUE.equals(e.get("verified"));
                    break;
                }
            }
        }
        String displayName = (name != null && !name.isBlank()) ? name : login;
        return new OAuthUserInfo(id, email, displayName, verified);
    }

    // [삭제] parseKakao 제거 (구글/깃허브로 전환). 원본 주석 보존:
    //    // Kakao userinfo: { id, kakao_account: { email, profile: { nickname } } }
    //    @SuppressWarnings("unchecked")
    //    private OAuthUserInfo parseKakao(Map<String, Object> body) {
    //        String providerUserId = String.valueOf(body.get("id"));
    //        Map<String, Object> account = (Map<String, Object>) body.getOrDefault("kakao_account", Map.of());
    //        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
    //        return new OAuthUserInfo(providerUserId, (String) account.get("email"), (String) profile.get("nickname"));
    //    }
    // [삭제] 위 parseKakao 삭제

    // provider 인가 URL 조립 (state 포함)
    private String buildAuthorizationUrl(OAuthProperties.Provider cfg, String state) {
        return cfg.getAuthorizationUri()
                + "?response_type=code"
                + "&client_id=" + enc(cfg.getClientId())
                + "&redirect_uri=" + enc(cfg.getRedirectUri())
                + "&scope=" + enc(cfg.getScope())
                + "&state=" + enc(state);
    }

    // 닉네임 미제공 provider 대비 fallback
    // [수정] provider 인자 추가 (fallback 이름 provider_id)
    private String resolveNickname(String provider, OAuthUserInfo info) {
        if (info.name() != null && !info.name().isBlank()) return info.name();
        return provider + "_" + info.providerUserId();
    }

    private SessionUser toSessionUser(User user) {
        return new SessionUser(user.getUserId(), user.getEmail(), user.getNickname(), user.getRole(), false, user.getProfileImageUrl());
    }

    private void requireLogin(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }

    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private String issueState(HttpSession session, String provider) {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String state = HexFormat.of().formatHex(bytes);
        session.setAttribute(OAUTH_STATE_KEY, provider + ":" + state);
        return state;
    }

    private void validateState(HttpSession session, String provider, String state) {
        Object value = session.getAttribute(OAUTH_STATE_KEY);
        if (!(value instanceof String stored) || !stored.equals(provider + ":" + state)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "OAuth state 검증에 실패했습니다.");
        }
    }

    // provider 사용자 식별 정보 (내부 전용 record)
    // [수정] emailVerified 추가 — 검증된 이메일만 기존 계정 연동에 사용
    private record OAuthUserInfo(String providerUserId, String email, String name, boolean emailVerified) {}

    // 세션에 임시 저장되는 연동 대기 정보 (세션 직렬화 대상 → Serializable)
    public record PendingSocialLink(String provider, String providerUserId, String providerEmail)
            implements Serializable {}
}
