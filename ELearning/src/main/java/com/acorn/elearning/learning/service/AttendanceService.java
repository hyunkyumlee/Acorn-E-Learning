package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.AttendanceRecordMapper;
import com.acorn.elearning.learning.model.AttendanceRecord;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 출석/streak 기록 서비스. */
@Service
public class AttendanceService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); // 출석 기준일은 KST 고정

    private final AttendanceRecordMapper attendanceRecordMapper;

    public AttendanceService(AttendanceRecordMapper attendanceRecordMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
    }

    /**
     * 오늘(KST) 출석을 1건 기록한다. 하루 1회만 인정하므로 오늘 이미 출석했으면 기존 기록을 반환한다.
     * streak는 직전 출석이 어제면 +1, 아니면 1로 시작한다.
     */
    @Transactional
    public AttendanceRecord recordAttendanceOnPracticePass(Long userId, Long qualifiedSetAttemptId) {
        LocalDate today = LocalDate.now(KST);

        AttendanceRecord latest = attendanceRecordMapper.findLatestByUserId(userId).orElse(null);

        // 오늘 이미 출석 → 그대로 반환
        if (latest != null && today.equals(latest.getAttendanceDate())) {
            return latest;
        }

        // streak: 직전 출석이 어제면 +1, 아니면 1
        int streak = 1;
        if (latest != null && latest.getAttendanceDate() != null
                && latest.getAttendanceDate().equals(today.minusDays(1))) {
            int prev = (latest.getStreakCount() != null) ? latest.getStreakCount() : 0;
            streak = prev + 1;
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setAttendanceDate(today);
        record.setStreakCount(streak);
        record.setQualifiedSetAttemptId(qualifiedSetAttemptId);
        attendanceRecordMapper.insert(record);
        return record;
    }
}
