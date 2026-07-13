package com.acorn.elearning.admin.mapper;

import org.apache.ibatis.annotations.Param;

public interface AdminContentMapper {

    int deactivateCurriculumNodesBySubjectId(@Param("subjectId") Long subjectId);

    int deactivateLessonsBySubjectId(@Param("subjectId") Long subjectId);

    int deactivateProblemsBySubjectId(@Param("subjectId") Long subjectId);

    int cancelActiveExamsBySubjectId(@Param("subjectId") Long subjectId);
}