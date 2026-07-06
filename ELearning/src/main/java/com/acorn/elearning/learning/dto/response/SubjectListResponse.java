package com.acorn.elearning.learning.dto.response;

import java.util.List;

/** GET /api/subjects 응답: 활성 과목 목록. */
public record SubjectListResponse(List<Item> subjects) {

    public record Item(Long subjectId, String subjectCode, String subjectName, String description) {}
}
