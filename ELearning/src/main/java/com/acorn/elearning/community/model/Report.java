    package com.acorn.elearning.community.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class Report {
        private Long reportId;
private String targetType;
private Long targetId;
private Long reporterId;
private String reasonCode;
private String status;
private Long handledBy;
private LocalDateTime handledAt;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
