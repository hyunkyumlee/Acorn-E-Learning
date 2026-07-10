package com.acorn.elearning.auth.service;

import com.acorn.elearning.auth.dto.request.LoginRequest;
import com.acorn.elearning.auth.dto.request.SignupRequest;
import com.acorn.elearning.auth.dto.response.UserSessionResponse;
import com.acorn.elearning.auth.form.LoginForm;
import com.acorn.elearning.auth.form.SignupForm;
import com.acorn.elearning.auth.mapper.UserCredentialMapper;
import com.acorn.elearning.auth.model.LoginUserRow;
import com.acorn.elearning.auth.model.UserCredential;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.payment.service.PaymentAccessService;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.mapper.UserSettingMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import com.acorn.elearning.user.model.UserSetting;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthService {
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserCredentialMapper userCredentialMapper;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;
    private final PaymentAccessService paymentAccessService;

    public AuthService(UserCredentialMapper userCredentialMapper, SessionService sessionService, PasswordEncoder passwordEncoder, UserMapper userMapper, UserSettingMapper userSettingMapper, UserLearningProfileMapper userLearningProfileMapper, PaymentAccessService paymentAccessService) {
        this.userCredentialMapper = userCredentialMapper;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.paymentAccessService = paymentAccessService;
    }

    public UserSessionResponse login(HttpSession session, LoginForm form) {
        return login(session, form.getEmail(), form.getPassword());
    }

    public UserSessionResponse login (HttpSession session, LoginRequest request) {
        return login(session, request.email(), request.password());
    }

    private UserSessionResponse login(HttpSession session, String email, String rawPassword) {
        LoginUserRow row = userCredentialMapper.findByLoginEmail(email).orElseThrow( () -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        if (!STATUS_ACTIVE.equals(row.getStatus())) {
            throw new BusinessException(ErrorCode.AUTH_SUSPENDED);
        }
        if (!passwordEncoder.matches(rawPassword, row.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        SessionUser sessionUser = toSessionUser(row);
        sessionService.saveUser(session, sessionUser);
        return sessionService.toLoginResponse(sessionUser);
    }

    public void logout(HttpSession session) {
        sessionService.logout(session);
    }

    private SessionUser toSessionUser(LoginUserRow row) {
        boolean premiumActive = paymentAccessService.hasPremiumAccess(row.getUserId());
        return new SessionUser(row.getUserId(), row.getEmail(), row.getNickname(), row.getRole(), premiumActive, row.getProfileImageUrl());
    }

    // ---- signup ----
    @Transactional
    public UserSessionResponse signup(HttpSession session, SignupForm form) {
        return signup(session, form.getEmail(), form.getPassword(), form.getNickname(), form.getPrimarySubjectId(), form.getLearningGoal());
    }

    @Transactional
    public UserSessionResponse signup(HttpSession session, SignupRequest request) {
        return signup(session, request.email(), request.password(), request.nickname(), request.primarySubjectId(), request.learningGoal());
    }

    // remember-me 쿠키에서 얻은 userId . SessionUser 만들어 세션에 저장 (자동 로그인 복원)
    //tokenVersion 인자 추가 - 쿠키의 버전이 현재 계정 버전과 같을 때만 복원 (비번 변경 후 옛 쿠키 거부)
    public void restoreSession(HttpSession session, Long userId, long tokenVersion) {
        userMapper.findById(userId)
                .filter(user -> STATUS_ACTIVE.equals(user.getStatus()))   // 정지/탈퇴 계정은 자동 복원 금지
                .filter(user -> tokenVersion == currentTokenVersion(userId)) // 버전 불일치 (비번 변경후)
                .ifPresent(user -> sessionService.saveUser(session, toSessionUser(user)));
    }
    // 발급(AuthController)과 복원(바로 위 코드)에서 같은 값을 계산해 비교하므로 public
    public long currentTokenVersion(Long userId) {
        return userCredentialMapper.findByUserId(userId)
                .map(c -> c.getPasswordUpdatedAt() == null ? 0L : c.getPasswordUpdatedAt().toEpochSecond(ZoneOffset.UTC)). orElse(0L);
    }

    private UserSessionResponse signup (HttpSession session, String email, String rawPassword, String nickname, Long primarySubjectId, String learningGoal) {
        if (userCredentialMapper.findByLoginEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
        }
        if (userMapper.existByNickname(nickname)) {
            throw new BusinessException(ErrorCode.AUTH_NICKNAME_DUPLICATED);
        }

        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setRole(SessionUser.ROLE_USER);
        user.setStatus(STATUS_ACTIVE);
        userMapper.insert(user);

        UserCredential credential = new UserCredential();
        credential.setUserId(user.getUserId());
        credential.setLoginEmail(email);
        credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        credential.setEmailVerifiedAt(LocalDateTime.now());
        userCredentialMapper.insert(credential);

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

        SessionUser sessionUser = toSessionUser(user);

        return sessionService.toSignupResponse(sessionUser);
    }

    private SessionUser toSessionUser(User user){
        boolean premiumActive = false;
        return new SessionUser(user.getUserId(), user.getEmail(), user.getNickname(), user.getRole(), premiumActive, user.getProfileImageUrl());
    }

}