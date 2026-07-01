package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminLessonManageRowResponse;
import com.acorn.elearning.learning.model.Lesson;

import java.util.List;

public interface AdminLessonMapper {

    List<AdminLessonManageRowResponse> findAll();
}
