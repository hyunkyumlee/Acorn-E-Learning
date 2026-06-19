package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LessonBookmark;
import java.util.List;
import java.util.Optional;

public interface LessonBookmarkMapper {
    Optional<LessonBookmark> findById(Long id);
    List<LessonBookmark> findAll();
    int insert(LessonBookmark model);
    int update(LessonBookmark model);
}
