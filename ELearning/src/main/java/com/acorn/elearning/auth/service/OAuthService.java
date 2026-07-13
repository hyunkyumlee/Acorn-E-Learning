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
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";   // [추가] 탈퇴 상태
    private static final String GITHUB_EMAILS_URI = "https://api.github.com/user/emails";
    private static final String PENDING_SOCIAL_SIGNUP_KEY = "PENDING_SOCIAL_SIGNUP";   // [추가] 소셜 회원가입 대기정보 세션 키

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

    // ================= AUTH-006: 로그인 callback =================
    @Transactional
    public String handleLoginCallback(String provider, String code, String state, HttpSession session) {
        OAuthProperties.Provider cfg = oAuthProperties.require(provider);
        validateState(session, provider, state);
        session.removeAttribute(OAUTH_STATE_KEY);

        OAuthUserInfo info = fetchUserInfo(provider, cfg, code);

        // (1) 이미 연동돼 있으면 그 회원으로 로그인
        SocialAccount linked = socialAccountMapper
                .findByProviderAndProviderUserId(provider, info.providerUserId()).orElse(null);

        if (linked != null) {
            User owner = userMapper.findById(linked.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND));

            // [수정] 탈퇴 계정이 아니면 기존대로 로그인.
            //        탈퇴(WITHDRAWN) 계정이면 로그인하지 않고 아래 (2) 회원가입(재가입) 경로로 진행
            if (!STATUS_WITHDRAWN.equals(owner.getStatus())) {
                if (!Boolean.TRUE.equals(linked.getIsActive())) {
                    reactivateSocialAccount(linked, info);
                }
                SessionUser sessionUser = toSessionUser(owner);
                sessionService.saveUser(session, sessionUser);
                return sessionUser.defaultRedirectPath();
            }
            // 탈퇴 계정 → fall through: 재가입 허용(회원가입 화면으로)
        }

        // (2) 미연동(신규) 또는 탈퇴 계정 재가입 → 즉시 생성하지 않고 provider 정보를 세션에 임시 저장 후 회원가입 화면으로
        String resolvedEmail = (info.email() != null && !info.email().isBlank())
                ? info.email()
                : provider + "_" + info.providerUserId() + "@social.knowva.local";
        PendingSocialSignup pending = new PendingSocialSignup(
                provider, info.providerUserId(), resolvedEmail, info.name(), info.emailVerified());
        session.setAttribute(PENDING_SOCIAL_SIGNUP_KEY, pending);
        return "/oauth/signup";
    }

    // ================= [추가] 소셜 회원가입 완료 (폼 클래스 없이 값으로 받음) =================
    @Transactional
    public String completeSocialSignup(HttpSession session, String nickname, Long primarySubjectId, String learningGoal) {
        Object value = session.getAttribute(PENDING_SOCIAL_SIGNUP_KEY);
        if (!(value instanceof PendingSocialSignup pending)) {
            throw new BusinessException(ErrorCode.AUTH_SOCIAL_PENDING_EXPIRED);   // 세션 만료/직접 접근
        }

        // [수정] 동일 소셜이 '탈퇴가 아닌' 계정에 이미 연결돼 있을 때만 중복 차단 (탈퇴 계정이면 재가입 허용)
        SocialAccount existing = socialAccountMapper
                .findByProviderAndProviderUserId(pending.provider(), pending.providerUserId()).orElse(null);
        if (existing != null) {
            User owner = userMapper.findById(existing.getUserId()).orElse(null);
            boolean ownerWithdrawn = owner != null && STATUS_WITHDRAWN.equals(owner.getStatus());
            if (!ownerWithdrawn) {
                session.removeAttribute(PENDING_SOCIAL_SIGNUP_KEY);
                throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 연결된 소셜 계정입니다.");
            }
        }

        // 닉네임 중복 검사 (탈퇴 시 닉네임이 마스킹되므로 예전 닉네임은 재사용 가능)
        if (userMapper.existByNickname(nickname)) {
            throw new BusinessException(ErrorCode.AUTH_NICKNAME_DUPLICATED);
        }

        User user = createSocialUser(pending, nickname, primarySubjectId, learningGoal);

        // social_accounts 연동 저장 (내부 record 재사용)
        OAuthUserInfo info = new OAuthUserInfo(
                pending.providerUserId(), pending.email(), pending.name(), pending.emailVerified());
        if (existing != null) {
            // [추가] 탈퇴 계정이 쓰던 소셜 row를 새 회원으로 재연결(repoint) — uk_social_provider_user 유니크 충돌 회피
            existing.setUserId(user.getUserId());
            existing.setIsActive(true);
            existing.setDisconnectedAt(null);
            existing.setProviderEmail(info.email());
            existing.setProviderEmailVerified(info.emailVerified());
            socialAccountMapper.update(existing);
        } else {
            insertSocialAccount(user.getUserId(), pending.provider(), info);   // 순수 신규만 insert
        }

        session.removeAttribute(PENDING_SOCIAL_SIGNUP_KEY);

        SessionUser sessionUser = toSessionUser(user);
        sessionService.saveUser(session, sessionUser);
        return sessionUser.defaultRedirectPath();
    }

    // [추가] 세션의 소셜 임시정보 이메일 (없으면 null) — 화면 표시/가드용
    public String pendingEmail(HttpSession session) {
        Object value = session.getAttribute(PENDING_SOCIAL_SIGNUP_KEY);
        return (value instanceof PendingSocialSignup pending) ? pending.email() : null;
    }

    // [추가] provider 이름을 닉네임 기본값으로 제안 (없으면 provider_아이디)
    public String pendingSuggestedNickname(HttpSession session) {
        Object value = session.getAttribute(PENDING_SOCIAL_SIGNUP_KEY);
        if (value instanceof PendingSocialSignup pending) {
            return (pending.name() != null && !pending.name().isBlank())
                    ? pending.name()
                    : pending.provider() + "_" + pending.providerUserId();
        }
        return "";
    }

    // [추가] 회원 탈퇴 시 해당 회원의 모든 소셜 계정 연동 해제(soft delete = 비활성화).
    //        행은 남겨두며, 이후 같은 소셜로 재가입하면 completeSocialSignup에서 새 회원으로 repoint된다.
    @Transactional
    public void deactivateSocialAccounts(Long userId) {
        for (SocialAccount account : socialAccountMapper.findByUserId(userId)) {
            if (Boolean.TRUE.equals(account.getIsActive())) {
                account.setIsActive(false);
                account.setDisconnectedAt(LocalDateTime.now());
                socialAccountMapper.update(account);
            }
        }
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
        SocialAccount existing = socialAccountMapper.findByProviderAndProviderUserId(provider, info.providerUserId()).orElse(null);

        if (existing != null) {
            if(Boolean.TRUE.equals(existing.getIsActive())) {
                throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 연결된 소셜 계정입니다.");
            }
            if (!existing.getUserId().equals(sessionUser.userId())) {
                throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "이미 다른 계정에 연결된 소셜 계정입니다.");
            }
            reactivateSocialAccount(existing, info);
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

    // [추가] 소셜 회원가입 값으로 신규 회원 생성 (비밀번호 credential 없음)
    private User createSocialUser(PendingSocialSignup pending, String nickname, Long primarySubjectId, String learningGoal) {
        User user = new User();
        user.setEmail(pending.email());
        user.setNickname(nickname);
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
        profile.setPrimarySubjectId(primarySubjectId);
        profile.setLearningGoal(learningGoal);
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

    // authorization code → access token
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
    private OAuthUserInfo parseGoogle(Map<String, Object> body) {
        boolean verified = Boolean.parseBoolean(String.valueOf(body.get("email_verified")));
        return new OAuthUserInfo(String.valueOf(body.get("sub")),
                (String) body.get("email"), (String) body.get("name"), verified);
    }

    // GitHub: /user + /user/emails (primary/verified)
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

    private void reactivateSocialAccount(SocialAccount row, OAuthUserInfo info) {
        row.setIsActive(true);
        row.setDisconnectedAt(null);
        row.setProviderEmail(info.email());
        row.setProviderEmailVerified(info.emailVerified());
        socialAccountMapper.update(row);
    }

    // provider 사용자 식별 정보 (내부 전용 record)
    private record OAuthUserInfo(String providerUserId, String email, String name, boolean emailVerified) {
    }

    // [추가] 소셜 인증은 끝났지만 아직 회원가입(DB 생성) 전인 임시 정보. 세션에만 잠시 보관.
    //        별도 파일을 만들지 않고 중첩 record로 선언. 세션 저장을 위해 Serializable.
    private record PendingSocialSignup(
            String provider,
            String providerUserId,
            String email,
            String name,
            boolean emailVerified
    ) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }
}