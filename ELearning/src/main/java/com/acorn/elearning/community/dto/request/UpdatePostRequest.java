package com.acorn.elearning.community.dto.request;

import com.acorn.elearning.community.form.PostForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000, message = "본문은 2,000자 이하로 입력해주세요.") String content,
        @NotNull Long subjectId,
        @NotBlank @Size(max = 30) String boardType,
        String idempotencyToken
) {
    public PostForm toForm() {
        PostForm form = new PostForm();
        form.setTitle(title);
        form.setContent(content);
        form.setSubjectId(subjectId);
        form.setBoardType(boardType);
        form.setIdempotencyToken(idempotencyToken);
        return form;
    }
}
