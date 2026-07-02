package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.form.SecurityForm;
import com.acorn.elearning.user.form.WithdrawUserForm;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;

    public UserService(UserMapper userMapper, UserLearningProfileMapper userLearningProfileMapper) {
        this.userMapper = userMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        return UserProfileResponse.of(user, learningProfile);
    }

    @Transactional
    public UserProfileResponse updateProfile(SessionUser sessionUser, ProfileForm form) {
        Long userId = requireUserId(sessionUser);
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));

        user.setNickname(normalizeRequired(form.getNickname()));
        userMapper.update(user);

        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        String learningGoal = normalizeOptional(form.getLearningGoal());
        if (learningProfile != null) {
            learningProfile.setLearningGoal(learningGoal);
            userLearningProfileMapper.update(learningProfile);
        } else if (learningGoal != null) {
            learningProfile = new UserLearningProfile();
            learningProfile.setUserId(userId);
            learningProfile.setLearningGoal(learningGoal);
            learningProfile.setCurrentLevelCode("BRONZE");
            learningProfile.setTotalScore(0);
            userLearningProfileMapper.insert(learningProfile);
        }

        return UserProfileResponse.of(user, learningProfile);
    }

    @Transactional
    public UserProfileResponse updateSecurity(SessionUser sessionUser, SecurityForm form) {
        Long userId = requireUserId(sessionUser);
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        String email = normalizeEmail(form.getEmail());
        userMapper.findByEmail(email)
                .filter(found -> !found.getUserId().equals(userId))
                .ifPresent(found -> {
                    throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "이미 사용 중인 이메일입니다.");
                });

        user.setEmail(email);
        userMapper.update(user);

        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        return UserProfileResponse.of(user, learningProfile);
    }

    @Transactional
    public UserProfileResponse withdraw(SessionUser sessionUser, WithdrawUserForm form) {
        Long userId = requireUserId(sessionUser);
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        if (!Boolean.TRUE.equals(form.getConfirmed())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "회원 탈퇴 확인에 동의해주세요.");
        }

        user.setStatus("WITHDRAWN");
        user.setWithdrawnAt(LocalDateTime.now());
        userMapper.update(user);

        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        return UserProfileResponse.of(user, learningProfile);
    }

    public Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }

    private String normalizeRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value).toLowerCase(Locale.ROOT);
    }
}
