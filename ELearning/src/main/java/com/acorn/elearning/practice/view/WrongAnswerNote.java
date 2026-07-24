package com.acorn.elearning.practice.view;

import java.io.Serializable;

/**
 * 오답 상세에서 다운로드하거나 커뮤니티 작성 화면으로 전달하는 사용자 소유의 복습 노트다.
 */
public record WrongAnswerNote(
        Long wrongAnswerId,
        Long subjectId,
        String fileName,
        String postTitle,
        String markdown
) implements Serializable {
}
