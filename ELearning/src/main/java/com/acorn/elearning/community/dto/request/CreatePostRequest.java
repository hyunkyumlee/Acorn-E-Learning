package com.acorn.elearning.community.dto.request;

import com.acorn.elearning.community.form.PostForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
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
