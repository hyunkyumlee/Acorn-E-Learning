package com.acorn.elearning.community.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {
    @NotBlank
    @Size(max = 1000)
    private String content;

    private Long parentCommentId;
    private String idempotencyToken;
}
