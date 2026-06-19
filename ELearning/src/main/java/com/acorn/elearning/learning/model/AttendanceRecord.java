        package com.acorn.elearning.learning.model;

        import java.time.LocalDate;
import java.time.LocalDateTime;
        import lombok.Getter;
        import lombok.Setter;

        @Getter
        @Setter
        public class AttendanceRecord {
            private Long attendanceId;
    private Long userId;
    private LocalDate attendanceDate;
    private Integer streakCount;
    private Long qualifiedSetAttemptId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
        }
