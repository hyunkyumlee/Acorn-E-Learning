package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.UserLessonProgress;
import com.acorn.elearning.learning.view.SubjectProgressRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface UserLessonProgressMapper {

    Optional<UserLessonProgress> findByUserAndLesson(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    List<UserLessonProgress> findByUserAndNode(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    int upsertTheoryCompleted(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    int countCompletedRequiredLessons(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    /**
     * 과목별 required 레슨 총수와 완료 수를 한 번에 집계한다(과목 하나당 한 행).
     * 완료 판정은 countCompletedRequiredLessons와 동일한 규약(theory + practice 둘 다)을 쓴다.
     * 과목 목록을 그릴 때 과목마다 노드를 순회하지 않기 위한 집계 쿼리다.
     */
    List<SubjectProgressRow> findLessonStatsBySubject(@Param("userId") Long userId);

    //문제풀이 상태값 저장용
    int upsertPracticePassed(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
}
