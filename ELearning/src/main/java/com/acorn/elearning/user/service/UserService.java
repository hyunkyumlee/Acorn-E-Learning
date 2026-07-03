package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.config.UploadProperties;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.form.SecurityForm;
import com.acorn.elearning.user.form.WithdrawUserForm;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserLearningProfileMapper userLearningProfileMapper;
    private final UploadProperties uploadProperties;

    public UserService(
            UserMapper userMapper,
            UserLearningProfileMapper userLearningProfileMapper,
            UploadProperties uploadProperties
    ) {
        this.userMapper = userMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.uploadProperties = uploadProperties;
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
        if (hasProfileImage(form.getProfileImage())) {
            user.setProfileImageUrl(storeProfileImage(userId, form.getProfileImage()));
        }
        userMapper.update(user);

        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        String learningGoal = normalizeOptional(form.getLearningGoal());
        if (learningProfile != null) {
            learningProfile.setLearningGoal(learningGoal);
            if (form.getPrimarySubjectId() != null) {
                learningProfile.setPrimarySubjectId(form.getPrimarySubjectId());
            }
            userLearningProfileMapper.update(learningProfile);
        } else if (learningGoal != null || form.getPrimarySubjectId() != null) {
            learningProfile = new UserLearningProfile();
            learningProfile.setUserId(userId);
            learningProfile.setPrimarySubjectId(form.getPrimarySubjectId());
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

    private boolean hasProfileImage(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String storeProfileImage(Long userId, MultipartFile file) {
        String extension = imageExtension(file.getContentType());
        String fileName = "user-" + userId + "-" + UUID.randomUUID() + "." + extension;
        Path directory = uploadBasePath().resolve("profile-images").toAbsolutePath().normalize();
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "프로필 이미지 파일명이 올바르지 않습니다.");
        }
        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), target);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "프로필 이미지를 저장할 수 없습니다.");
        }
        return "/mypage/profile-images/" + fileName;
    }

    private Path uploadBasePath() {
        String configuredPath = uploadProperties == null ? null : uploadProperties.basePath();
        String basePath = configuredPath == null || configuredPath.isBlank() ? "./uploads" : configuredPath;
        return Paths.get(basePath).toAbsolutePath().normalize();
    }

    private String imageExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "PNG, JPG, WEBP 이미지만 업로드할 수 있습니다.");
    }
}
