    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class ScoreEvent {
        private Long scoreEventId;
        private Long userId;
        private Long subjectId;
        private String sourceType;
        private Long sourceId;
        private Integer scoreDelta;
        private String reasonCode;
        private String idempotencyKey;
        private LocalDateTime createdAt;
    }
