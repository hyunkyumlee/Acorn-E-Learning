package com.acorn.elearning.practice.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeAnswerForm {
   /*
    @NotBlank
    private String skeletonValue = "TODO";
    private Long id;
*/

    // 1. 문제 식별자 (어떤 문제에 대한 답인지)
    @NotNull
    private Long problemId;

    // 2. 사용자가 선택한 답안
    @NotBlank
    private String submittedAnswer;

    // 3. 중복방지? -- 기존 필드 (필요시 유지)
    private String idempotencyToken;
}
