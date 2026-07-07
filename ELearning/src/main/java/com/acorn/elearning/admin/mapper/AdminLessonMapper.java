package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminLessonManageRowResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminLessonMapper {

    List<AdminLessonManageRowResponse> findAll();

    int deleteById(@Param("lessonId") Long lessonId);

    int deleteBookmarksByLessonId(@Param("lessonId") Long lessonId);
}
