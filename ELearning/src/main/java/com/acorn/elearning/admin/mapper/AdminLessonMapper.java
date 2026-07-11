package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminLessonManageRowResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminLessonMapper {

    List<AdminLessonManageRowResponse> findAll();

    int deleteById(@Param("lessonId") Long lessonId);

    List<AdminLessonManageRowResponse> findPage(@Param("limit") int limit,
                                                @Param("offset") int offset,
                                                @Param("keyword") String keyword,
                                                @Param("subjectName") String subjectName,
                                                @Param("curriculumTitle") String curriculumTitle,
                                                @Param("levelCode") String levelCode,
                                                @Param("isActive") Boolean isActive);

    long countAll(@Param("keyword") String keyword,
                  @Param("subjectName") String subjectName,
                  @Param("curriculumTitle") String curriculumTitle,
                  @Param("levelCode") String levelCode,
                  @Param("isActive") Boolean isActive);
    int deleteBookmarksByLessonId(@Param("lessonId") Long lessonId);
}
