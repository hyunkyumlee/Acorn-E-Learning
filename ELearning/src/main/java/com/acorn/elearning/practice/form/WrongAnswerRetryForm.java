package com.acorn.elearning.practice.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WrongAnswerRetryForm {
    //@NotBlank
    //private String skeletonValue = "TODO";

    //private Long id; URL PATH에서 받으므로 없어도 됨

    @NotBlank(message = "답안을 입력해주세요.")
    private String submittedAnswer;
    private String idempotencyToken;

}