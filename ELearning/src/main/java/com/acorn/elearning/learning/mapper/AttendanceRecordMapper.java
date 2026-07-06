package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AttendanceRecordMapper {
    Optional<AttendanceRecord> findById(Long id);
    List<AttendanceRecord> findAll();
    /** 대시보드 streak용: 특정 사용자의 가장 최근 출석 1건. */
    Optional<AttendanceRecord> findLatestByUserId(Long userId);
    /** 이번 주 출석 표시용: [startDate, endDate] 구간의 출석 레코드. */
    List<AttendanceRecord> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
    int insert(AttendanceRecord model);
    int update(AttendanceRecord model);
}
