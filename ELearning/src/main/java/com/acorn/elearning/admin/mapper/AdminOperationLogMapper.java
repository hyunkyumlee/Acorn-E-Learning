package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminOperationLogPageResponse;
import com.acorn.elearning.admin.model.AdminOperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface AdminOperationLogMapper {
    Optional<AdminOperationLog> findById(Long id);
    List<AdminOperationLog> findAll();
    int insert(AdminOperationLog model);
    int update(AdminOperationLog model);


    List<AdminOperationLogPageResponse> findPage(
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("targetType") String targetType,
            @Param("actionCategory") String actionCategory
    );

    long countAll(
            @Param("targetType") String targetType,
            @Param("actionCategory") String actionCategory
    );


}
