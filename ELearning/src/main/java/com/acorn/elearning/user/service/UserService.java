package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.service.EnrollmentService;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.storage.ObjectStorage;
import com.acorn.elearning.storage.StorageException;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.form.SecurityForm;
import com.acorn.elearning.user.form.WithdrawUserForm;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;
import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.io.IOException;
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
    private final EnrollmentService enrollmentService;
    private final ObjectStorage objectStorage;

    public UserService(
            UserMapper userMapper,
            UserLearningProfileMapper userLearningProfileMapper,
            EnrollmentService enrollmentService,
            ObjectStorage objectStorage
    ) {
        this.userMapper = userMapper;
        this.userLearningProfileMapper = userLearningProfileMapper;
        this.enrollmentService = enrollmentService;
        this.objectStorage = objectStorage;
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

        String previousProfileImageUrl = user.getProfileImageUrl();
        boolean profileImageChanged = false;

        user.setNickname(normalizeRequired(form.getNickname()));
        if (form.isResetProfileImage()) {
            user.setProfileImageUrl(null);
            profileImageChanged = true;
        } else if (hasProfileImage(form.getProfileImage())) {
            user.setProfileImageUrl(storeProfileImage(userId, form.getProfileImage()));
            profileImageChanged = true;
        }
        try {
            userMapper.update(user);
        } catch (RuntimeException exception) {
            if (profileImageChanged && user.getProfileImageUrl() != null) {
                deleteStoredProfileImage(user.getProfileImageUrl());
            }
            throw exception;
        }
        if (profileImageChanged) {
            deleteStoredProfileImage(previousProfileImageUrl);
        }

        UserLearningProfile learningProfile = userLearningProfileMapper.findByUserId(userId).orElse(null);
        String learningGoal = normalizeOptional(form.getLearningGoal());
        validatePrimarySubject(userId, form.getPrimarySubjectId());
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
        user.setNickname("탈퇴회원_" + userId);                         // 정하 - 추가 : nickname 마스킹 — 비식별화 + 닉네임 점유 해제(userId라 유니크 충돌 없음)
        user.setEmail("withdrawn_" + userId + "@masked.local");        // 정하 - 추가 : email 마스킹 — NFR-010 식별 정보 마스킹
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

    private void validatePrimarySubject(Long userId, Long primarySubjectId) {
        if (primarySubjectId != null && !enrollmentService.isEnrolled(userId, primarySubjectId)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "수강신청한 과목만 대표 과목으로 선택할 수 있습니다.");
        }
    }

    private boolean hasProfileImage(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String storeProfileImage(Long userId, MultipartFile file) {
        String extension = imageExtension(file.getContentType());
        String fileName = "user-" + userId + "-" + UUID.randomUUID() + "." + extension;
        String key = "profile-images/" + fileName;
        try (java.io.InputStream input = file.getInputStream()) {
            objectStorage.put(key, input, file.getSize(), file.getContentType());
        } catch (IOException | StorageException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "프로필 이미지를 저장할 수 없습니다.");
        }
        return "/mypage/profile-images/" + fileName;
    }

    private void deleteStoredProfileImage(String imageUrl) {
        String imagePathPrefix = "/mypage/profile-images/";
        if (imageUrl == null || !imageUrl.startsWith(imagePathPrefix)) {
            return;
        }
        String fileName = imageUrl.substring(imagePathPrefix.length());
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            return;
        }

        try {
            objectStorage.delete("profile-images/" + fileName);
        } catch (IOException | StorageException ignored) {
            // DB의 프로필 이미지는 이미 변경되었으므로, 삭제 실패 파일은 다음 정리 작업에서 처리한다.
        }
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
