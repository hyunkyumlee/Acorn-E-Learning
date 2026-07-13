package com.acorn.elearning.admin.mapper;

import com.acorn.elearning.admin.dto.response.AdminChartPointResponse;
import com.acorn.elearning.admin.dto.response.AdminStatsResponse;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.community.model.Report;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminDashboardMapper {
    long countTotalUsers();
    long countTodayUsers();
    long countAllLearning();
    long countTodayLearning();
    long countAllSubmissions();
    long countTodaySubmissions();
    long countPendingReports();
    long countAllActiveUsers();
    long countTodayActiveUsers();
    long countAllExamAttempts();
    long countTodayExamAttempts();

    List<Report> findRecentReports();
    List<Notice> findRecentNotices();

    List<AdminChartPointResponse> findDailyLearningActivity(@Param("periodUnit") String periodUnit);

    List<AdminChartPointResponse> findSubjectCompletionCounts(
            @Param("periodUnit") String periodUnit,
            @Param("subject") String subject,
            @Param("range") String range
    );

    List<AdminChartPointResponse> findSubjectAverageExamScores(
            @Param("periodUnit") String periodUnit,
            @Param("range") String range
    );

    List<AdminStatsResponse.TableRow> findStatsTableRows(
            @Param("subject") String subject,
            @Param("range") String range
    );
}
