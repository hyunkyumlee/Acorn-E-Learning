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
        LoginUserRow row = userCredentialMapper.findByEmail(email).orElseThrow( () -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
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
        return new SessionUser( row.getUserId(), row.getEmail(), row.getNickname(), row.getRole(), premiumActive);
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

    private UserSessionResponse signup (HttpSession session, String email, String rawPassword, String nickname, Long primarySubjectId, String learningGoal) {
        if (userMapper.findByEmail(email).isPresent()) {
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
        credential.setPasswordHash(passwordEncoder.encode(rawPassword));
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
        return new SessionUser(user.getUserId(), user.getEmail(), user.getNickname(), user.getRole(), premiumActive);
    }



//    public Map<String, Object> stub(String action) {
//        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
//        // LoginForm form = ...; SignupForm signupForm = ...;
//        // UserCredential credential = userCredentialMapper.findByEmail(form.getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
//        // SessionUser sessionUser = sessionService.createSessionUser(credential.userId());
//        // return Map.of("session", sessionUser);
//        return Map.of(
//                "action", action,
//                "status", "SKELETON",
//                "redirectUrlByRole", Map.of(
//                        SessionUser.ROLE_USER, "/learning",
//                        SessionUser.ROLE_ADMIN, "/admin"));
//    }
}
