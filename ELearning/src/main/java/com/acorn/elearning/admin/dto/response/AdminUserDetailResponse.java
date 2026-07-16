package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long userId,
        String email,
        String nickname,
        String subjectName,
        String gradeCode,
        Integer totalScore,
        String role,
        String status,
        LocalDateTime createdAt,
        Integer progressCount,
        Integer completedCount,
        Double averageProgressRate,
        Integer postCount,
        Integer commentCount,
        Integer reportedCount,
        Integer receivedReportCount
) {
}
