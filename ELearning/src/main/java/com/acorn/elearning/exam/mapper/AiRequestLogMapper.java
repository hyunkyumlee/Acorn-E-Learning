package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.AiRequestLog;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AiRequestLogMapper {
    Optional<AiRequestLog> findById(Long id);
    List<AiRequestLog> findByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    List<AiRequestLog> findAll();
    int insert(AiRequestLog model);
    int update(AiRequestLog model);
}
