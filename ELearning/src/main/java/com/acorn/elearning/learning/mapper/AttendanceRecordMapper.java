package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.AttendanceRecord;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordMapper {
    Optional<AttendanceRecord> findById(Long id);
    List<AttendanceRecord> findAll();
    /** 대시보드 streak용: 특정 사용자의 가장 최근 출석 1건. */
    Optional<AttendanceRecord> findLatestByUserId(Long userId);
    int insert(AttendanceRecord model);
    int update(AttendanceRecord model);
}
