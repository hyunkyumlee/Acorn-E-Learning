package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 난이도 unlock 기록 서비스. */
@Service
public class UnlockService {

    public static final String SOURCE_AI_EXAM_PASS = "AI_EXAM_PASS";
    public static final String SOURCE_ADMIN_ADJUST = "ADMIN_ADJUST";

    private final UserLevelUnlockMapper unlockMapper;

    public UnlockService(UserLevelUnlockMapper unlockMapper) {
        this.unlockMapper = unlockMapper;
    }

    /** (user, subject, level) unlock을 기록한다. 이미 있으면 기존 행을 반환한다(중복 방지). */
    @Transactional
    public UserLevelUnlock unlock(Long userId, Long subjectId, String levelCode,
                                  String unlockSource, Long unlockedByExamId) {
        UserLevelUnlock existing =
                unlockMapper.findByUserSubjectLevel(userId, subjectId, levelCode).orElse(null);
        if (existing != null) {
            return existing;
        }

        UserLevelUnlock unlock = new UserLevelUnlock();
        unlock.setUserId(userId);
        unlock.setSubjectId(subjectId);
        unlock.setLevelCode(levelCode);
        unlock.setUnlockSource(unlockSource);
        unlock.setUnlockedByExamId(unlockedByExamId);
        unlock.setUnlockedAt(LocalDateTime.now());
        unlockMapper.insert(unlock);
        return unlock;
    }

    /**
     * AI 코딩테스트 통과 시: 현재 레벨의 "다음 레벨"을 unlock한다.
     * 진행 순서 BRONZE → SILVER → GOLD. 현재가 GOLD(최고)면 다음이 없어 아무 것도 하지 않고 null을 반환한다.
     */
    @Transactional
    public UserLevelUnlock unlockNextLevel(Long userId, Long subjectId, String currentLevelCode, Long examId) {
        String nextLevel = nextLevelOf(currentLevelCode);
        if (nextLevel == null) {
            return null;
        }
        return unlock(userId, subjectId, nextLevel, SOURCE_AI_EXAM_PASS, examId);
    }

    /** 레벨 진행 순서에서 다음 레벨. 다음이 없으면(GOLD 또는 미지정) null. */
    private static String nextLevelOf(String levelCode) {
        if ("BRONZE".equals(levelCode)) {
            return "SILVER";
        }
        if ("SILVER".equals(levelCode)) {
            return "GOLD";
        }
        return null;
    }
}
