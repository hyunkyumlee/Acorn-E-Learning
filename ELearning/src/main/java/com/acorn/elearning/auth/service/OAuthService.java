package com.acorn.elearning.auth.service;

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
import com.acorn.elearning.auth.mapper.UserCredentialMapper;
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
    private static final String GITHUB_EMAILS_URI = "https://api.github.com/user/emails";   // [추가] GitHub 이메일 2차 조회

    private final SocialAccountMapper socialAccountMapper;
    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;
    private final SessionService sessionService;
    private final OAuthProperties oAuthProperties;
    private final UserCredentialMapper userCredentialMapper; //최소 인증수단 유지 룰 검증용
    private final SecureRandom secureRandom = new SecureRandom();
    private final RestClient restClient = RestClient.create();

    public OAuthService(SocialAccountMapper socialAccountMapper,
                        UserMapper userMapper,
                        UserSettingMapper userSettingMapper,
                        UserLearningProfileMapper userLearningProfileMapper,
                        SessionService sessionService,
                        OAuthProperties oAuthProperties, UserCredentialMapper userCredentialMapper) {
        this.socialAccountMapper = socialAccountMapper;
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.sessionService = sessionService;
        this.oAuthProperties = oAuthProperties;
        this.userCredentialMapper = userCredentialMapper;
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

        if (linked != null) {
            if(!Boolean.TRUE.equals(linked.getIsActive())) {
                reactivateSocialAccount(linked, info); //비활성(soft-deleted)일 때만 재활성화 -> uk_social_provider_user 충돌 회피
            }

            User user = userMapper.findById(linked.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND));
            SessionUser sessionUser = toSessionUser(user);
            sessionService.saveUser(session, sessionUser);
            return sessionUser.defaultRedirectPath();
        }

        // (2) 미연동 -> 새 계정을 즉시 생성하고 바로 로그인시킨다.
        //     users.email 중복 여부는 검사하지 않는다 (정책상 허용).
        //     registerSocialUser 안에서 UserSetting/UserLearningProfile까지 이미 생성하므로 여기서는 호출만 한다.
        User user = registerSocialUser(provider, info);
        insertSocialAccount(user.getUserId(), provider, info);

        SessionUser sessionUser = toSessionUser(user);
        sessionService.saveUser(session, sessionUser);
        return sessionUser.defaultRedirectPath();
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
        // 기존 행(활성/비활성) 존재 여부로 묻기
        SocialAccount existing = socialAccountMapper.findByProviderAndProviderUserId(provider, info.providerUserId()).orElse(null);

        if (existing != null) {
            if(Boolean.TRUE.equals(existing.getIsActive())) {
                throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 연결된 소셜 계정입니다.");
            }

            //soft-deleted 행 : 다른 계정 소유면 거부, 같은 계정이면 재활성화
            if (!existing.getUserId().equals(sessionUser.userId())) {
                throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 다른 계정에 연결된 소셜 계정입니다.");
            }
            reactivateSocialAccount(existing, info); // insert 대신 재활성화 -> uk_social_user_provider 충돌 회피
            return "/settings/social";
        }
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
        List<SocialAccount> accounts = socialAccountMapper.findByUserId(sessionUser.userId());

        SocialAccount account = accounts.stream()
                .filter(a -> provider.equals(a.getProvider()) && Boolean.TRUE.equals(a.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "연결된 소셜 계정이 없습니다."));

        long activeSocialCount = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getIsActive())).count();
        boolean hasPassword = userCredentialMapper.findByUserId(sessionUser.userId())
                        .map(c->c.getPasswordHash() != null && !c.getPasswordHash().isBlank())
                                .orElse(false);

        if (activeSocialCount <= 1 && !hasPassword) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이 소셜 계정은 유일한 로그인 수단이라 해제할 수 없습니다. 먼저 비밀번호를 설정하거나 다른 소셜 계정을 연결하세요.");
        }


        account.setIsActive(false);
        account.setDisconnectedAt(LocalDateTime.now());
        socialAccountMapper.update(account);
        return SocialAccountResponse.from(account);
    }

    // ------------------------- 내부 helper -------------------------

    // 소셜 전용 신규 회원 생성 (비밀번호 credential 없음)
    //provider 인자 추가 + email null 대비(대체 이메일 생성) — users.email NOT NULL 대응
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
        account.setProvider(provider.toLowerCase(java.util.Locale.ROOT));
        account.setProviderUserId(info.providerUserId());
        account.setProviderEmail(info.email());
        account.setProviderEmailVerified(info.emailVerified());
        account.setIsActive(true);
        socialAccountMapper.insert(account);
    }

    // provider에서 token 교환 후 userinfo 조회 (google/github 분기)
    // github 분기 추가 + Accept 헤더
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
    // Accept: application/json 헤더 추가 — GitHub는 없으면 form 응답이라 JSON 파싱 실패
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
    // email_verified 반영
    private OAuthUserInfo parseGoogle(Map<String, Object> body) {
        boolean verified = Boolean.parseBoolean(String.valueOf(body.get("email_verified")));
        return new OAuthUserInfo(String.valueOf(body.get("sub")),
                (String) body.get("email"), (String) body.get("name"), verified);
    }

    // GitHub: /user( id, login, name, email ) + /user/emails( primary, verified )
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
    // provider 인자 추가 (fallback 이름 provider_id)
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
    // emailVerified 추가 — 검증된 이메일만 기존 계정 연동에 사용
    private record OAuthUserInfo(String providerUserId, String email, String name, boolean emailVerified) {
    }

    private void reactivateSocialAccount(SocialAccount row, OAuthUserInfo info) {
        row.setIsActive(true);
        row.setDisconnectedAt(null);
        row.setProviderEmail(info.email());
        row.setProviderEmailVerified(info.emailVerified());
        //update가 is_active/disconnected_at/provider_email(_verified) 갱신
        socialAccountMapper.update(row);
    }
}