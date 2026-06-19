package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.Lesson;
import java.util.List;
import java.util.Optional;

public interface LessonMapper {
    Optional<Lesson> findById(Long id);
    List<Lesson> findAll();
    int insert(Lesson model);
    int update(Lesson model);
}
