package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.ReportPageResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminReportMapper {

    List<ReportPageResponse.ReportItem> findAll();

    int updateStatus(@Param("reportId") Long reportId, @Param("status") String status);
}
