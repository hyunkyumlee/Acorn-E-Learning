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
}
