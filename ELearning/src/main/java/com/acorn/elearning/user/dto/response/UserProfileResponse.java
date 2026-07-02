package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.user.model.User;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        String status,
        String profileImageUrl,
        LocalDateTime lastLoginAt,
        LocalDateTime withdrawnAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long learningProfileId,
        Long primarySubjectId,
        String learningGoal,
        String currentLevelCode,
        Integer totalScore,
        String gradeCode,
        String roleLabel,
        String statusLabel,
        String createdAtLabel,
        String lastLoginAtLabel,
        String learningLanguageLabel,
        String learningGoalLabel,
        String currentLevelLabel,
        String gradeLabel
) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static UserProfileResponse of(User user, UserLearningProfile learningProfile) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProfileImageUrl(),
                user.getLastLoginAt(),
                user.getWithdrawnAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                learningProfile == null ? null : learningProfile.getProfileId(),
                learningProfile == null ? null : learningProfile.getPrimarySubjectId(),
                learningProfile == null ? null : learningProfile.getLearningGoal(),
                learningProfile == null ? null : learningProfile.getCurrentLevelCode(),
                learningProfile == null ? null : learningProfile.getTotalScore(),
                learningProfile == null ? null : learningProfile.getGradeCode(),
                roleLabel(user.getRole()),
                statusLabel(user.getStatus()),
                formatDate(user.getCreatedAt()),
                formatDateTime(user.getLastLoginAt()),
                learningLanguageLabel(learningProfile),
                learningGoalLabel(learningProfile),
                codeLabel(learningProfile == null ? null : learningProfile.getCurrentLevelCode()),
                codeLabel(learningProfile == null ? null : learningProfile.getGradeCode())
        );
    }

    private static String roleLabel(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return "관리자";
        }
        if ("ROLE_USER".equals(role)) {
            return "학습자";
        }
        return hasText(role) ? role : "-";
    }

    private static String statusLabel(String status) {
        if ("ACTIVE".equals(status)) {
            return "활성";
        }
        if ("WITHDRAWN".equals(status)) {
            return "탈퇴";
        }
        if ("SUSPENDED".equals(status)) {
            return "정지";
        }
        return hasText(status) ? status : "-";
    }

    private static String learningLanguageLabel(UserLearningProfile learningProfile) {
        if (learningProfile == null || learningProfile.getPrimarySubjectId() == null) {
            return "설정 안 됨";
        }
        return "주 과목 #" + learningProfile.getPrimarySubjectId();
    }

    private static String learningGoalLabel(UserLearningProfile learningProfile) {
        if (learningProfile == null || !hasText(learningProfile.getLearningGoal())) {
            return "학습 목표가 아직 없습니다.";
        }
        return learningProfile.getLearningGoal();
    }

    private static String codeLabel(String code) {
        return hasText(code) ? code : "-";
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
