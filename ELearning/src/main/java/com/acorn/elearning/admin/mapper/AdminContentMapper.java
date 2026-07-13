package com.acorn.elearning.admin.mapper;

import org.apache.ibatis.annotations.Param;

public interface AdminContentMapper {

    int deactivateCurriculumNodesBySubjectId(@Param("subjectId") Long subjectId);
    int deactivateLessonsBySubjectId(@Param("subjectId") Long subjectId);
    int deactivateProblemsBySubjectId(@Param("subjectId") Long subjectId);
    int cancelActiveExamsBySubjectId(@Param("subjectId") Long subjectId);

    int backupContentStatusesBySubjectId(@Param("subjectId") Long subjectId);
    int restoreCurriculumNodesBySubjectId(@Param("subjectId") Long subjectId);
    int restoreLessonsBySubjectId(@Param("subjectId") Long subjectId);
    int restoreProblemsBySubjectId(@Param("subjectId") Long subjectId);
    int deleteContentStatusBackupsBySubjectId(@Param("subjectId") Long subjectId);
}