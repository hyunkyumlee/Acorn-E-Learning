package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminChartPointResponse;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.community.model.Report;

import java.util.List;

public interface AdminDashboardMapper {
    long countTotalUsers();
    long countTodayLearning();
    long countTodaySubmissions();
    long countPendingReports();

    List<Report> findRecentReports();
    List<Notice> findRecentNotices();

    List<AdminChartPointResponse> findDailyLearningActivity();
    List<AdminChartPointResponse> findSubjectCompletionCounts();
}
