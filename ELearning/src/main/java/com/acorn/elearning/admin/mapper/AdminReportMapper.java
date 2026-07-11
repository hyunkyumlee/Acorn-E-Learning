package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.ReportPageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminReportMapper {

    List<ReportPageResponse.ReportItem> findAll();
    List<ReportPageResponse.ReportItem> findPage(@Param("limit") int limit,
                                                 @Param("offset") int offset,
                                                 @Param("targetType") String targetType,
                                                 @Param("status") String status,
                                                 @Param("reportDate") String reportDate);
    long countAll(@Param("targetType") String targetType,
                  @Param("status") String status,
                  @Param("reportDate") String reportDate);
    int updateStatus(@Param("reportId") Long reportId, @Param("status") String status);
}
