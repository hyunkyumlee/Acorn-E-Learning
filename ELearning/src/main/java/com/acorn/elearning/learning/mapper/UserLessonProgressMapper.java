package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.UserLessonProgress;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserLessonProgressMapper {

    Optional<UserLessonProgress> findByUserAndLesson(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    List<UserLessonProgress> findByUserAndNode(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    int upsertTheoryCompleted(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    int countCompletedRequiredLessons(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    //문제풀이 상태값 저장용
    int upsertPracticePassed(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
}
