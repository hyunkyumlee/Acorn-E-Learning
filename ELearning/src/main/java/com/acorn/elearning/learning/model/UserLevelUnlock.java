    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class UserLevelUnlock {
        private Long unlockId;
private Long userId;
private Long subjectId;
private String levelCode;
private String unlockSource;
private Long unlockedByExamId;
private LocalDateTime unlockedAt;
private LocalDateTime createdAt;
    }
