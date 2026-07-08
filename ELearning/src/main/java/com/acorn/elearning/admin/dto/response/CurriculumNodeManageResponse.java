package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record CurriculumNodeManageResponse(
        Long nodeId,
        String subjectName,
        String levelCode,
        String nodeType,
        String title,
        Boolean isActive
){}
