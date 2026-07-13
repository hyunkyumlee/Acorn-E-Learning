package com.acorn.elearning.admin.service;


import java.util.List;


import com.acorn.elearning.admin.dto.response.AdminChartPointResponse;
import com.acorn.elearning.admin.dto.response.AdminStatsResponse;
import com.acorn.elearning.admin.mapper.AdminDashboardMapper;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.community.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AdminDashboardMapper dm;

    private boolean isTodayScope(String summaryScope) {
        return "today".equalsIgnoreCase(summaryScope);
    }

    public long countUsers(String summaryScope) {
        return isTodayScope(summaryScope) ? dm.countTodayUsers() : dm.countTotalUsers();
    }

    public long countActiveUsers(String summaryScope) {
        return isTodayScope(summaryScope) ? dm.countTodayActiveUsers() : dm.countAllActiveUsers();
    }

    public long countLearning(String summaryScope) {
        return isTodayScope(summaryScope) ? dm.countTodayLearning() : dm.countAllLearning();
    }

    public long countSubmissions(String summaryScope) {
        return isTodayScope(summaryScope) ? dm.countTodaySubmissions() : dm.countAllSubmissions();
    }

    public long countExamAttempts(String summaryScope) {
        return isTodayScope(summaryScope) ? dm.countTodayExamAttempts() : dm.countAllExamAttempts();
    }


    //전체 사용자 수
    public long countTotalUsers(){

        return dm.countTotalUsers();
    }

    //오늘 학습량
    public long countTodayLearning(){
        return dm.countTodayLearning();

    }

    //오늘 문제풀이 수
    public long countTodaySubmissions(){
        return dm.countTodaySubmissions();
    }

    //신고 대기 수
    public long countPendingReports(){
        return dm.countPendingReports();
    }

    //활성 사용자 수
    public long countTodayActiveUsers(){return dm.countTodayActiveUsers();}

    //시험 응시 수
    public long countTodayExamAttempts(){return dm.countTodayExamAttempts();}

    //최근 신고 목록

    public List<Report> findRecentReports(){

        return dm.findRecentReports();
    }

    //최근 공지사항 목록
    public List<Notice> findRecentNotices(){
        return dm.findRecentNotices();
    }


    private List<AdminChartPointResponse> applyPercent(List<AdminChartPointResponse> points) {
        long maxValue = points.stream()
                .mapToLong(AdminChartPointResponse::getValue)
                .max()
                .orElse(0);

        if (maxValue == 0) {
            return points;
        }

        for (AdminChartPointResponse point : points) {
            int percent = (int) Math.round((double) point.getValue() / maxValue * 72);
            point.setPercent(percent);
        }

        return points;
    }


    public List<AdminChartPointResponse> dailyLearningChart(){
        return dailyLearningChart(null, null);
    }

    public List<AdminChartPointResponse> dailyLearningChart(String periodUnit, String range){
      return applyPercent(dm.findDailyLearningActivity(periodUnit));
    }

    public List<AdminChartPointResponse> subjectCompleteChart(){
        return subjectCompleteChart(null, null, null);
    }

    public List<AdminChartPointResponse> subjectCompleteChart(String periodUnit, String subject, String range){
        return applyPercent(dm.findSubjectCompletionCounts(periodUnit, subject, range));
    }

    public List<AdminChartPointResponse> subjectExamScoreChart(String periodUnit, String range) {
        return applyPercent(dm.findSubjectAverageExamScores(periodUnit, range));
    }

    public List<AdminStatsResponse.TableRow> findStatsTableRows(String subject, String range){
        return dm.findStatsTableRows(subject, range);
    }

    public AdminStatsResponse getStats(String summaryScope, String periodUnit, String subject){
        AdminStatsResponse.Summary summary = new AdminStatsResponse.Summary(
                countUsers(summaryScope),
                countActiveUsers(summaryScope),
                countLearning(summaryScope),
                countSubmissions(summaryScope),
                countExamAttempts(summaryScope)
        );

        return new AdminStatsResponse(
                summary,
                dailyLearningChart(periodUnit, null),
                subjectCompleteChart(periodUnit, subject, null),
                subjectExamScoreChart(periodUnit, null),
                findStatsTableRows(subject, null)
        );
    }
}
