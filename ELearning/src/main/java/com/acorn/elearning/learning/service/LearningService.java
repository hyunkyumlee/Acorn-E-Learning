package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.AttendanceRecordMapper;
import com.acorn.elearning.learning.mapper.LearningProfileReadMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.model.AttendanceRecord;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class LearningService {

    private final SubjectMapper subjectMapper;
    private final LearningProfileReadMapper learningProfileReadMapper;
    private final LearningProgressMapper learningProgressMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;

    public LearningService(SubjectMapper subjectMapper,
                           LearningProfileReadMapper learningProfileReadMapper,
                           LearningProgressMapper learningProgressMapper,
                           AttendanceRecordMapper attendanceRecordMapper) {
        this.subjectMapper = subjectMapper;
        this.learningProfileReadMapper = learningProfileReadMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
    }

    /**
     * 학습 메인 화면용: 활성(is_active=1) 과목 목록을 sort_order 순으로 조회한다.
     * SubjectMapper.findAll()이 sort_order ASC로 정렬해 주므로 여기서는 활성 필터만 적용한다.
     */
    public List<Subject> getActiveSubjects() {
        return subjectMapper.findAll().stream()
                .filter(subject -> Boolean.TRUE.equals(subject.getIsActive()))
                .toList();
    }

    /**
     * 학습 메인(SR-003) 대시보드 데이터를 조립한다.
     * - user_learning_profiles: 주 과목 / 현재 레벨 / 등급 / 누적 점수
     * - learning_progress: 주 과목 노드들의 진행률 평균(0~100 정수)
     * - attendance_records: 최근 출석의 streak, 오늘 출석 여부
     * 출석/streak는 ranking 점수와 무관하다(분담 기준).
     */
    public LearningDashboardView getLearningHome(SessionUser user) {
        Long userId = user.userId();

        UserLearningProfile profile = learningProfileReadMapper.findByUserId(userId).orElse(null);
        Long primarySubjectId = (profile != null) ? profile.getPrimarySubjectId() : null;

        int progressRate = averageProgressRate(userId, primarySubjectId);

        int streakCount = 0;
        boolean attendedToday = false;
        AttendanceRecord latest = attendanceRecordMapper.findLatestByUserId(userId).orElse(null);
        if (latest != null) {
            streakCount = (latest.getStreakCount() != null) ? latest.getStreakCount() : 0;
            attendedToday = LocalDate.now().equals(latest.getAttendanceDate());
        }

        return new LearningDashboardView(
                user.nickname(),
                primarySubjectId,
                (profile != null) ? profile.getCurrentLevelCode() : null,
                (profile != null) ? profile.getGradeCode() : null,
                (profile != null && profile.getTotalScore() != null) ? profile.getTotalScore() : 0,
                progressRate,
                streakCount,
                attendedToday
        );
    }

    /** 주 과목 진행 행들의 progress_rate 평균을 0~100 정수로 환산. 데이터가 없으면 0. */
    private int averageProgressRate(Long userId, Long subjectId) {
        if (subjectId == null) {
            return 0;
        }
        List<LearningProgress> rows = learningProgressMapper.findByUserIdAndSubjectId(userId, subjectId);
        double avg = rows.stream()
                .map(LearningProgress::getProgressRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0d);
        return (int) Math.round(avg);
    }
}
