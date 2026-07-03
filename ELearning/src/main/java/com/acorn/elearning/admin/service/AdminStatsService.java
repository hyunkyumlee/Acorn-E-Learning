package com.acorn.elearning.admin.service;


import java.util.List;


import com.acorn.elearning.admin.dto.response.AdminChartPointResponse;
import com.acorn.elearning.admin.mapper.AdminDashboardMapper;
import com.acorn.elearning.admin.model.Notice;
import com.acorn.elearning.community.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AdminDashboardMapper dm;


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
            int percent = (int) Math.round((double) point.getValue() / maxValue * 88);
            point.setPercent(percent);
        }

        return points;
    }

    //
    public List<AdminChartPointResponse> dailyLearningChart(){
      return applyPercent(dm.findDailyLearningActivity());
    }

    public List<AdminChartPointResponse> subjectCompleteChart(){
        return applyPercent(dm.findSubjectCompletionCounts());
    }

}
