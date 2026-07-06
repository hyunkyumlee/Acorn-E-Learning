package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.model.AdminOperationLog;
import java.util.List;
import java.util.Optional;

public interface AdminOperationLogMapper {
    Optional<AdminOperationLog> findById(Long id);
    List<AdminOperationLog> findAll();
    int insert(AdminOperationLog model);
    int update(AdminOperationLog model);


}
