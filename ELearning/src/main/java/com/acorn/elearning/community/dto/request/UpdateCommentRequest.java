package com.acorn.elearning.community.dto.request;

import com.acorn.elearning.community.form.CommentForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(@NotBlank @Size(max = 1000) String content) {
    public CommentForm toForm() {
        CommentForm form = new CommentForm();
        form.setContent(content);
        return form;
    }
}
