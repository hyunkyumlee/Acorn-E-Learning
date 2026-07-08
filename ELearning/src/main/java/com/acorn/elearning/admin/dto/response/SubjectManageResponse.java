package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record SubjectManageResponse(
        Long subjectId,
        String subjectName,
        String description,
        Boolean isActive,
        LocalDateTime createdAt
){

}
