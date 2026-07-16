package com.acorn.elearning.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreeCodingRunRequest(
        @NotBlank(message = "실행할 코드가 필요합니다.")
        @Size(max = 20_000, message = "코드는 20,000자 이하로 입력해 주세요.")
        String source,
        @Size(max = 4_000, message = "표준 입력은 4,000자 이하로 입력해 주세요.")
        String input
) {}
