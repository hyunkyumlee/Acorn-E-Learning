package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.ReportPageResponse;

import java.util.List;

public interface AdminReportMapper {

    List<ReportPageResponse.ReportItem> findAll();
}
