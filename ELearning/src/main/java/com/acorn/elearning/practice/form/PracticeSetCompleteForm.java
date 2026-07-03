package com.acorn.elearning.practice.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PracticeSetCompleteForm {

    //@NotBlank
    //private String skeletonValue = "TODO";
    //private Long id;

    // 1. 세트 ID (어떤 세트를 완료하는지 식별)
    @NotNull
    private Long setAttemptId;

    // 2. 제출한 답안 리스트 (PracticeAnswerForm 내부 클래스 SingleAnswer의 리스트)
    @NotEmpty
    private List<PracticeAnswerForm.SingleAnswer> answers;

    // 3.중복 제출 방지용 --기존필드 유지
    private String idempotencyToken;

}
