package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.AttendanceRecordMapper;
import com.acorn.elearning.learning.mapper.LearningProfileReadMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.AttendanceRecord;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.learning.view.LearningDashboardView;
import com.acorn.elearning.learning.view.OnboardingProfileView;
import com.acorn.elearning.learning.view.SubjectCardView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class LearningService {

    /** 레벨 코드를 낮은 난이도 → 높은 난이도 순으로 본다. 과목별 "현재 레벨" = 연 레벨 중 가장 높은 것. */
    private static final List<String> LEVEL_ORDER = List.of("BRONZE", "SILVER", "GOLD");

    /** 출석 기준일은 AttendanceService와 동일하게 KST로 고정한다. */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SubjectMapper subjectMapper;
    private final LearningProfileReadMapper learningProfileReadMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final UserLevelUnlockMapper userLevelUnlockMapper;
    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;

    public LearningService(SubjectMapper subjectMapper,
                           LearningProfileReadMapper learningProfileReadMapper,
                           AttendanceRecordMapper attendanceRecordMapper,
                           UserLevelUnlockMapper userLevelUnlockMapper,
                           EnrollmentService enrollmentService,
                           ProgressService progressService) {
        this.subjectMapper = subjectMapper;
        this.learningProfileReadMapper = learningProfileReadMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.userLevelUnlockMapper = userLevelUnlockMapper;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
    }

    /** 과목 단건 조회. 비활성 과목은 학습 대상이 아니므로 없는 것으로 본다. */
    public Subject getSubject(Long subjectId) {
        return subjectMapper.findById(subjectId)
                .filter(subject -> Boolean.TRUE.equals(subject.getIsActive()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "과목 정보를 찾을 수 없습니다."));
    }

    /**
     * 레벨 테스트 화면(문항·결과)에 표시할 요약. 대상 과목과 저장된 학습 목표로 만든다.
     * 화면에 담긴 값이 아니라 실제 과목을 기준으로 해야 과목 소개 화면에서 바로 들어온 경우에도 과목명이 나온다.
     */
    public OnboardingProfileView getLevelTestProfile(SessionUser user, Long subjectId) {
        String subjectName = subjectMapper.findById(subjectId)
                .map(Subject::getSubjectName)
                .orElse(null);
        String learningGoal = learningProfileReadMapper.findByUserId(user.userId())
                .map(UserLearningProfile::getLearningGoal)
                .orElse(null);
        return new OnboardingProfileView(user.nickname(), subjectId, subjectName, learningGoal);
    }

    /**
     * 좌측 과목 목록을 조립한다. 과목마다 수강 여부 · 현재 레벨 · 진행률을 함께 담는다.
     * unlock과 진행률은 과목별로 다시 조회하지 않고 사용자 단위로 한 번씩만 조회해 과목에 나눠 붙인다.
     */
    public List<SubjectCardView> getSubjectCards(Long userId, Long selectedSubjectId) {
        Set<Long> enrolledSubjectIds = enrollmentService.getEnrolledSubjectIds(userId);
        Map<Long, Integer> progressBySubject = progressService.computeSubjectProgress(userId);
        Map<Long, String> levelBySubject = highestUnlockedLevels(userId);

        return getActiveSubjects().stream()
                .map(subject -> {
                    Long subjectId = subject.getSubjectId();
                    return new SubjectCardView(
                            subjectId,
                            subject.getSubjectCode(),
                            subject.getSubjectName(),
                            subject.getDescription(),
                            enrolledSubjectIds.contains(subjectId),
                            subjectId.equals(selectedSubjectId),
                            levelBySubject.get(subjectId),
                            progressBySubject.getOrDefault(subjectId, 0));
                })
                .toList();
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

    /** 비활성 포함 전체 과목(sort_order 순). REST /api/subjects?activeOnly=false 용. */
    public List<Subject> getAllSubjects() {
        return subjectMapper.findAll();
    }

    /**
     * 학습 메인 대시보드의 사용자 프로필/출석 정보를 조립한다.
     * - user_learning_profiles: 주 과목 / 등급 / 누적 점수
     * - user_level_unlocks: 선택 과목에서 현재 열린 가장 높은 레벨(없으면 주 과목 프로필 레벨 fallback)
     * - attendance_records: 최근 출석의 streak, 오늘 출석 여부
     * 로드맵 진행률(행성 완료수·%)은 선택 과목 기준으로 ProgressService가 별도 계산한다.
     * 출석/streak는 ranking 점수와 무관하다(분담 기준).
     */
    public LearningDashboardView getLearningHome(SessionUser user) {
        return getLearningHome(user, null);
    }

    /**
     * 학습 메인/API가 선택한 과목의 실제 해금 레벨을 함께 조회한다.
     * 프로필의 current_level_code는 레벨 테스트 배정값이라 이후 게이트 통과 진도를 반영하지 않는다.
     */
    public LearningDashboardView getLearningHome(SessionUser user, Long selectedSubjectId) {
        Long userId = user.userId();

        UserLearningProfile profile = learningProfileReadMapper.findByUserId(userId).orElse(null);
        Long primarySubjectId = (profile != null) ? profile.getPrimarySubjectId() : null;
        Long displaySubjectId = selectedSubjectId != null ? selectedSubjectId : primarySubjectId;
        String unlockedLevel = displaySubjectId == null
                ? null
                : highestUnlockedLevels(userId).get(displaySubjectId);
        String currentLevelCode = unlockedLevel != null
                ? unlockedLevel
                : (displaySubjectId != null && displaySubjectId.equals(primarySubjectId) && profile != null
                        ? profile.getCurrentLevelCode()
                        : null);

        int streakCount = 0;
        boolean attendedToday = false;
        AttendanceRecord latest = attendanceRecordMapper.findLatestByUserId(userId).orElse(null);
        if (latest != null && latest.getAttendanceDate() != null) {
            LocalDate today = LocalDate.now(KST);
            LocalDate lastAttended = latest.getAttendanceDate();
            attendedToday = today.equals(lastAttended);
            // 마지막 출석이 오늘도 어제도 아니면 연속은 이미 끊긴 것 → 지난 streak_count를 그대로 보여주지 않는다.
            if (attendedToday || lastAttended.equals(today.minusDays(1))) {
                streakCount = (latest.getStreakCount() != null) ? latest.getStreakCount() : 0;
            }
        }

        return new LearningDashboardView(
                user.nickname(),
                primarySubjectId,
                currentLevelCode,
                (profile != null) ? profile.getGradeCode() : null,
                (profile != null && profile.getTotalScore() != null) ? profile.getTotalScore() : 0,
                streakCount,
                attendedToday
        );
    }

    private Map<Long, String> highestUnlockedLevels(Long userId) {
        Map<Long, String> levelBySubject = new HashMap<>();
        for (UserLevelUnlock unlock : userLevelUnlockMapper.findByUser(userId)) {
            if (unlock.getSubjectId() == null || unlock.getLevelCode() == null) {
                continue;
            }
            levelBySubject.merge(unlock.getSubjectId(), unlock.getLevelCode(),
                    (current, candidate) ->
                            LEVEL_ORDER.indexOf(candidate) > LEVEL_ORDER.indexOf(current) ? candidate : current);
        }
        return levelBySubject;
    }
}
