    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class LessonBookmark {
        private Long bookmarkId;
private Long userId;
private Long lessonId;
private LocalDateTime createdAt;
    }
