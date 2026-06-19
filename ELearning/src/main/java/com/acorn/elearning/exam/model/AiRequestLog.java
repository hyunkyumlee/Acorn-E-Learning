    package com.acorn.elearning.exam.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class AiRequestLog {
        private Long aiRequestLogId;
private String targetType;
private Long targetId;
private String requestType;
private String status;
private Integer retryNo;
private String requestPayload;
private String responsePayload;
private String errorCode;
private String errorMessage;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
