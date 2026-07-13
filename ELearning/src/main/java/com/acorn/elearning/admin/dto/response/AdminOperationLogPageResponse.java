package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record AdminOperationLogPageResponse(
        Long logId,
        String adminEmail,
        String adminNickname,
        String targetType,
        String actionType,
        Long targetId,
        String resultStatus,
        LocalDateTime createdAt
) {}
