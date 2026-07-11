package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.learning.model.CurriculumNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminCurriculumNodeMapper {

    List<CurriculumNode> findPage(@Param("limit") int limit,
                                  @Param("offset") int offset,
                                  @Param("keyword") String keyword,
                                  @Param("subjectId") Long subjectId,
                                  @Param("levelCode") String levelCode);





    long countAll(@Param("keyword") String keyword,
                  @Param("subjectId") Long subjectId,
                  @Param("levelCode") String levelCode);
}
