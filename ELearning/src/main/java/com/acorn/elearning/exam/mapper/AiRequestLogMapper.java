package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.AiRequestLog;
import java.util.List;
import java.util.Optional;

public interface AiRequestLogMapper {
    Optional<AiRequestLog> findById(Long id);
    List<AiRequestLog> findAll();
    int insert(AiRequestLog model);
    int update(AiRequestLog model);
}
