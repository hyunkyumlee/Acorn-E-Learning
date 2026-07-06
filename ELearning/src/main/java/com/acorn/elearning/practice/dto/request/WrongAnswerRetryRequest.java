package com.acorn.elearning.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record WrongAnswerRetryRequest(
        @NotBlank String requestId,
        Map<String, Object> payload
) {
    public static WrongAnswerRetryRequest from(String requestId, String submittedAnswer, String idempotencyToken) {
        return new WrongAnswerRetryRequest(
                requestId,
                Map.of(
                        "submittedAnswer", submittedAnswer,
                        "idempotencyToken", idempotencyToken == null ? "" : idempotencyToken
                )
        );
    }


}
