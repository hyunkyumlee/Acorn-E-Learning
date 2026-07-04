package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LessonBookmark;
import com.acorn.elearning.learning.view.LessonBookmarkItemView;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface LessonBookmarkMapper {
    Optional<LessonBookmark> findById(Long id);
    List<LessonBookmark> findAll();
    Optional<LessonBookmark> findByUserAndLesson(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
    int insert(LessonBookmark model);
    int update(LessonBookmark model);
    int deleteByUserAndLesson(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
    long countByUser(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
    List<LessonBookmarkItemView> findPageByUser(@Param("userId") Long userId,
                                                @Param("subjectId") Long subjectId,
                                                @Param("limit") int limit,
                                                @Param("offset") int offset);
}
