package com.acorn.elearning.learning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acorn.elearning.learning.mapper.AttendanceRecordMapper;
import com.acorn.elearning.learning.mapper.LearningProfileReadMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.model.UserLearningProfile;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LearningServiceDashboardLevelTest {

    @Test
    void dashboardUsesPrimarySubjectUnlockInsteadOfStaleProfileLevel() {
        LearningProfileReadMapper profileMapper = mock(LearningProfileReadMapper.class);
        AttendanceRecordMapper attendanceMapper = mock(AttendanceRecordMapper.class);
        UserLevelUnlockMapper unlockMapper = mock(UserLevelUnlockMapper.class);
        LearningService learningService = new LearningService(
                mock(SubjectMapper.class),
                profileMapper,
                attendanceMapper,
                unlockMapper,
                mock(EnrollmentService.class),
                mock(ProgressService.class)
        );
        UserLearningProfile profile = new UserLearningProfile();
        profile.setPrimarySubjectId(1L);
        profile.setCurrentLevelCode("GOLD");
        UserLevelUnlock bronzeUnlock = new UserLevelUnlock();
        bronzeUnlock.setSubjectId(1L);
        bronzeUnlock.setLevelCode("BRONZE");

        when(profileMapper.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(attendanceMapper.findLatestByUserId(7L)).thenReturn(Optional.empty());
        when(unlockMapper.findByUser(7L)).thenReturn(List.of(bronzeUnlock));

        assertEquals(
                "BRONZE",
                learningService.getLearningHome(new SessionUser(7L, "user@example.test", "사용자", "ROLE_USER", false))
                        .currentLevelCode()
        );
    }

    @Test
    void dashboardUsesSelectedSubjectUnlockInsteadOfPrimaryProfileLevel() {
        LearningProfileReadMapper profileMapper = mock(LearningProfileReadMapper.class);
        AttendanceRecordMapper attendanceMapper = mock(AttendanceRecordMapper.class);
        UserLevelUnlockMapper unlockMapper = mock(UserLevelUnlockMapper.class);
        LearningService learningService = new LearningService(
                mock(SubjectMapper.class),
                profileMapper,
                attendanceMapper,
                unlockMapper,
                mock(EnrollmentService.class),
                mock(ProgressService.class)
        );
        UserLearningProfile profile = new UserLearningProfile();
        profile.setPrimarySubjectId(1L);
        profile.setCurrentLevelCode("GOLD");
        UserLevelUnlock javaUnlock = new UserLevelUnlock();
        javaUnlock.setSubjectId(1L);
        javaUnlock.setLevelCode("GOLD");
        UserLevelUnlock pythonUnlock = new UserLevelUnlock();
        pythonUnlock.setSubjectId(2L);
        pythonUnlock.setLevelCode("SILVER");

        when(profileMapper.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(attendanceMapper.findLatestByUserId(7L)).thenReturn(Optional.empty());
        when(unlockMapper.findByUser(7L)).thenReturn(List.of(javaUnlock, pythonUnlock));

        assertEquals(
                "SILVER",
                learningService.getLearningHome(
                        new SessionUser(7L, "user@example.test", "사용자", "ROLE_USER", false),
                        2L
                ).currentLevelCode()
        );
    }
}
