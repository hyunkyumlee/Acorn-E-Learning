package com.acorn.elearning.practice.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PracticeAnswerForm {
   /*
    @NotBlank
    private String skeletonValue = "TODO";
    private Long id;
*/
    // 1. 여러 문제 답안
    @NotEmpty(message = "제출된 답안이 없습니다.")
    private List<@Valid SingleAnswer> answers;

    // 2. 내부용 답안 정보 클래스
    @Getter
    @Setter
    public static class SingleAnswer {
        // 2-1. 문제 ID
        @NotNull(message = "문제 ID는 필수입니다.")
        private Long problemId;
        // 2-2. 제출답안
        @NotBlank(message = "답안을 입력해주세요.")
        private String submittedAnswer;
    }

    // 3. 중복방지? -- 기존 필드 (필요시 유지)
    private String idempotencyToken;

}
