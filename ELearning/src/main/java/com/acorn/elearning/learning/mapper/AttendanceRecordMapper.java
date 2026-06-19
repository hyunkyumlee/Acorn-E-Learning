package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.AttendanceRecord;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordMapper {
    Optional<AttendanceRecord> findById(Long id);
    List<AttendanceRecord> findAll();
    int insert(AttendanceRecord model);
    int update(AttendanceRecord model);
}
