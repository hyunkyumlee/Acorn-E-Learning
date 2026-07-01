package com.acorn.elearning.admin.dto.response;

public record AdminProblemManageRowResponse(
        Long problemId,
        Long subjectId,
        Long nodeId,
        String subjectName,
        String curriculumTitle,
        String problemType,
        String question,
        String answerText,
        String difficultyCode,
        Boolean isActive
) { }
