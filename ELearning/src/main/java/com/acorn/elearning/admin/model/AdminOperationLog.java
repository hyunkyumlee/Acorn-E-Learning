    package com.acorn.elearning.admin.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class AdminOperationLog {
        private Long logId;
        private Long adminId;
        private String actionType;
        private String targetType;
        private Long targetId;
        private String resultStatus;
        private LocalDateTime createdAt;
    }
