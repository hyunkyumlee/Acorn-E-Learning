package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.Report;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface ReportMapper {
    Optional<Report> findById(@Param("id") Long id);
    List<Report> findAll();
    long countByTargetAndReporter(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("reporterId") Long reporterId
    );
    int insert(Report model);
    int update(Report model);
}
