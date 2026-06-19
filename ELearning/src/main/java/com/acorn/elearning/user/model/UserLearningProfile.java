    package com.acorn.elearning.user.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class UserLearningProfile {
        private Long profileId;
private Long userId;
private Long primarySubjectId;
private String learningGoal;
private String currentLevelCode;
private Integer totalScore;
private String gradeCode;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
