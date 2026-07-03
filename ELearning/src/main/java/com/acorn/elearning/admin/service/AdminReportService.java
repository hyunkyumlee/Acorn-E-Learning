package com.acorn.elearning.admin.service;

import java.util.List;
import java.util.Map;

import com.acorn.elearning.admin.dto.response.ReportPageResponse;
import com.acorn.elearning.admin.mapper.AdminReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportMapper rm;

    public ReportPageResponse findPage(){
        List<ReportPageResponse.ReportItem> reports = rm.findAll();
        return new ReportPageResponse(reports);
    }
}
