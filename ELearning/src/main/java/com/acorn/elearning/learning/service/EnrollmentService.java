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
    /** 레벨 진행 순서(LevelTestService와 같은 규약). 하위 레벨부터 채워 연다. */
    private static final List<String> LEVEL_ORDER = List.of("BRONZE", "SILVER", "GOLD");
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
     * 수강 중인데 연 레벨이 하나도 없는 과목인지 판정한다.
     *
     * 열린 레벨이 없는 상태는 두 가지다.
     *   1) 레벨 테스트로 시작하고 아직 채점하지 않았다 → 테스트를 마쳐야 레벨이 열린다.
     *   2) 기초 시작인데 unlock 기록만 없다 → 레벨을 열어 주지 않던 예전 온보딩으로 시작했거나
     *      프로필 레벨만 심어 둔 계정이다. 이 상태로 테스트에 보내면 기초 시작을 고른 사용자가 계속 되돌려진다.
     * 둘의 구분자는 start_mode뿐이다(프로필 current_level_code는 NOT NULL DEFAULT라 미응시를 못 가린다).
     *
     * 2)는 프로필 레벨까지 unlock을 채워 복구한다. 학습 가능 여부는 user_level_unlocks만 보므로
     * 프로필이 GOLD인데 unlock이 없으면 화면은 GOLD인데 학습은 403이 된다. 레벨 테스트 채점과 같은 규약으로
     * 판정 레벨까지의 하위 레벨을 모두 연다.
     *
     * 수강 기록이 아예 없으면 아직 시작 방식을 고르지 않은 것이므로 복구 대상이 아니다.
     * 여기서 걸러내지 않으면 시작 전 사용자에게 최저 레벨이 열려 잠긴 로드맵이 성립하지 않는다.
     */
    @Transactional
    public boolean requiresLevelTest(Long userId, Long subjectId) {
        if (!unlockMapper.findByUserAndSubject(userId, subjectId).isEmpty()) {
            return false;
        }

        UserSubjectEnrollment enrollment =
                enrollmentMapper.findByUserAndSubject(userId, subjectId).orElse(null);
        if (enrollment == null) {
            return true;
        }

        boolean startedWithLevelTest = START_MODE_LEVEL_TEST.equals(enrollment.getStartMode());
        if (startedWithLevelTest && levelTestService.hasQuestions(subjectId)) {
            return true;
        }

        for (String levelCode : levelsUpTo(profileLevelOf(userId, subjectId))) {
            unlockService.unlock(userId, subjectId, levelCode, SOURCE_ENROLLMENT, null);
        }
        return false;
    }

    /**
     * 복구 기준 레벨. 프로필 레벨은 주 과목의 레벨이므로 다른 과목에는 적용하지 않고 최저 레벨로 연다.
     */
    private String profileLevelOf(Long userId, Long subjectId) {
        UserLearningProfile profile = profileMapper.findByUserId(userId).orElse(null);
        if (profile == null
                || !subjectId.equals(profile.getPrimarySubjectId())
                || profile.getCurrentLevelCode() == null) {
            return LOWEST_LEVEL_CODE;
        }
        return profile.getCurrentLevelCode();
    }

    /** 최저 레벨부터 기준 레벨(포함)까지. 기준 레벨을 모르면 최저 레벨만. */
    private static List<String> levelsUpTo(String levelCode) {
        int index = LEVEL_ORDER.indexOf(levelCode);
        return (index < 0) ? List.of(LOWEST_LEVEL_CODE) : LEVEL_ORDER.subList(0, index + 1);
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
     *
     * 레벨을 연 적이 없는 계정은 보정하지 않는다. 가입 때 고른 관심 과목만으로 수강 상태를 만들어 버리면
     * 시작 방식(기초 시작 · 레벨 테스트)을 고르기도 전에 학습이 시작된 것으로 처리되기 때문이다.
     */
    @Transactional
    public void ensureBackfill(Long userId) {
        if (!enrollmentMapper.findByUser(userId).isEmpty()) {
            return;
        }

        List<UserLevelUnlock> unlocks = unlockMapper.findByUser(userId);
        if (unlocks.isEmpty()) {
            return;
        }

        Set<Long> subjectIds = new LinkedHashSet<>();

        UserLearningProfile profile = profileMapper.findByUserId(userId).orElse(null);
        if (profile != null && profile.getPrimarySubjectId() != null) {
            subjectIds.add(profile.getPrimarySubjectId());
        }

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
