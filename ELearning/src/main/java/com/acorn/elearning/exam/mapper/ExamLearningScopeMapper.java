package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.ExamLearningScopeItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExamLearningScopeMapper {
    List<ExamLearningScopeItem> findCompletedLessonScope(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("levelCode") String levelCode);

    List<ExamLearningScopeItem> findPassedPracticeScope(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("levelCode") String levelCode);

    int countAvailableScopeItems(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("levelCode") String levelCode);

    int countIncompleteRequiredLessons(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("levelCode") String levelCode
    );
}
