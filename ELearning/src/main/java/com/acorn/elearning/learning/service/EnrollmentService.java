package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.mapper.UserSubjectEnrollmentMapper;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.learning.model.UserSubjectEnrollment;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 과목 수강신청 서비스.
 * 수강 중인 과목만 학습을 진행할 수 있고, 신청하지 않은 과목은 잠금으로 표시된다.
 */
@Service
public class EnrollmentService {

    /** 기초부터 시작: 신청 즉시 최저 레벨을 연다. */
    public static final String START_MODE_BASIC = "BASIC";
    /** 레벨 테스트 응시: 신청 시점에는 레벨을 열지 않고, 테스트 결과가 레벨을 연다. */
    public static final String START_MODE_LEVEL_TEST = "LEVEL_TEST";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String LOWEST_LEVEL_CODE = "BRONZE";
    /** 수강신청으로 생긴 unlock의 출처. AI 코딩테스트(AI_EXAM_PASS)와 구분한다. */
    private static final String SOURCE_ENROLLMENT = "ENROLLMENT";

    private final UserSubjectEnrollmentMapper enrollmentMapper;
    private final UserLevelUnlockMapper unlockMapper;
    private final UserLearningProfileMapper profileMapper;
    private final UnlockService unlockService;
    private final LevelTestService levelTestService;

    public EnrollmentService(UserSubjectEnrollmentMapper enrollmentMapper,
                             UserLevelUnlockMapper unlockMapper,
                             UserLearningProfileMapper profileMapper,
                             UnlockService unlockService,
                             LevelTestService levelTestService) {
        this.enrollmentMapper = enrollmentMapper;
        this.unlockMapper = unlockMapper;
        this.profileMapper = profileMapper;
        this.unlockService = unlockService;
        this.levelTestService = levelTestService;
    }

    /**
     * 과목을 수강 신청한다. (user, subject) UNIQUE 기준으로 멱등 — 이미 신청한 과목이면 기존 행을 반환한다.
     * startMode가 BASIC이면 최저 레벨(BRONZE)을 함께 연다. LEVEL_TEST면 레벨을 열지 않는다
     * (레벨 테스트 채점 결과가 판정 등급까지 열어 준다).
     */
    @Transactional
    public UserSubjectEnrollment enroll(Long userId, Long subjectId, String startMode) {
        UserSubjectEnrollment existing =
                enrollmentMapper.findByUserAndSubject(userId, subjectId).orElse(null);
        if (existing == null) {
            existing = new UserSubjectEnrollment();
            existing.setUserId(userId);
            existing.setSubjectId(subjectId);
            existing.setStatus(STATUS_ACTIVE);
            existing.setStartMode(startMode);
            existing.setEnrolledAt(LocalDateTime.now());
            enrollmentMapper.insert(existing);
        }

        if (START_MODE_BASIC.equals(startMode)) {
            unlockService.unlock(userId, subjectId, LOWEST_LEVEL_CODE, SOURCE_ENROLLMENT, null);
        }
        return existing;
    }

    /**
     * 수강 중인데 연 레벨이 하나도 없는 과목인지 판정한다. 레벨 테스트로 시작하고 아직 채점하지 않은 상태다.
     * 이 상태로 로드맵을 열면 전 레벨이 잠긴 화면만 보이므로 테스트를 먼저 마쳐야 한다.
     * 다만 문항이 없는 과목은 테스트로 갈 수 없으므로, 최저 레벨을 열어 기초부터 학습하게 한다.
     */
    @Transactional
    public boolean requiresLevelTest(Long userId, Long subjectId) {
        if (!unlockMapper.findByUserAndSubject(userId, subjectId).isEmpty()) {
            return false;
        }
        if (levelTestService.hasQuestions(subjectId)) {
            return true;
        }
        unlockService.unlock(userId, subjectId, LOWEST_LEVEL_CODE, SOURCE_ENROLLMENT, null);
        return false;
    }

    /** 수강 중(ACTIVE)인 과목 id 집합. 과목 잠금 판정에 쓴다. */
    public Set<Long> getEnrolledSubjectIds(Long userId) {
        return enrollmentMapper.findActiveByUser(userId).stream()
                .map(UserSubjectEnrollment::getSubjectId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean isEnrolled(Long userId, Long subjectId) {
        return getEnrolledSubjectIds(userId).contains(subjectId);
    }

    /**
     * 수강신청 도입 이전부터 학습해 온 사용자가 자기 과목에서 잠기지 않도록 보정한다.
     * 수강 기록이 하나도 없을 때만 동작하며, 프로필의 주 과목과 이미 레벨을 연 과목을 수강 상태로 만든다.
     * 이미 수강 기록이 있으면 아무 것도 하지 않는다(사용자가 스스로 해지한 과목을 되살리지 않기 위함).
     */
    @Transactional
    public void ensureBackfill(Long userId) {
        if (!enrollmentMapper.findByUser(userId).isEmpty()) {
            return;
        }

        Set<Long> subjectIds = new LinkedHashSet<>();

        UserLearningProfile profile = profileMapper.findByUserId(userId).orElse(null);
        if (profile != null && profile.getPrimarySubjectId() != null) {
            subjectIds.add(profile.getPrimarySubjectId());
        }

        List<UserLevelUnlock> unlocks = unlockMapper.findByUser(userId);
        for (UserLevelUnlock unlock : unlocks) {
            subjectIds.add(unlock.getSubjectId());
        }

        for (Long subjectId : subjectIds) {
            UserSubjectEnrollment row = new UserSubjectEnrollment();
            row.setUserId(userId);
            row.setSubjectId(subjectId);
            row.setStatus(STATUS_ACTIVE);
            row.setStartMode(START_MODE_BASIC);
            row.setEnrolledAt(LocalDateTime.now());
            enrollmentMapper.insert(row);
        }
    }
}
