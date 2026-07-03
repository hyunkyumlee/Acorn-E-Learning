package com.acorn.elearning.learning.view;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonBookmarkItemView {
    private Long bookmarkId;
    private Long lessonId;
    private String lessonTitle;
    private Long nodeId;
    private String nodeTitle;
    private Long subjectId;
    private String levelCode;
    private LocalDateTime bookmarkedAt;
}
