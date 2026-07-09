package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminProblemManageRowResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminProblemMapper {

    List<AdminProblemManageRowResponse> findAll();
    List<AdminProblemManageRowResponse> findPage(@Param("limit") int limit,
                                                 @Param("offset") int offset,
                                                 @Param("keyword") String keyword,
                                                 @Param("subjectId") Long subjectId,
                                                 @Param("nodeId") Long nodeId,
                                                 @Param("problemType") String problemType,
                                                 @Param("difficultyCode") String difficultyCode,
                                                 @Param("isActive") Boolean isActive);

    long countAll(@Param("keyword") String keyword,
                  @Param("subjectId") Long subjectId,
                  @Param("nodeId") Long nodeId,
                  @Param("problemType") String problemType,
                  @Param("difficultyCode") String difficultyCode,
                  @Param("isActive") Boolean isActive);

    int deleteById(@Param("problemId") Long problemId);
}
