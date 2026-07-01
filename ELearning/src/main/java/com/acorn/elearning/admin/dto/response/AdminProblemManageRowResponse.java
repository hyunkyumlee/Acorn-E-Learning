package com.acorn.elearning.admin.dto.response;

public record AdminProblemManageRowResponse(
        Long problemId,
        String subjectName,
        String curriculumTitle,
        String problemType,
        String question,
        String difficultyCode,
        Boolean isActive
) { }
