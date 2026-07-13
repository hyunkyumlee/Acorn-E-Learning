package com.acorn.elearning.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTossPaymentOrderRequest(
        @NotBlank String productCode,
        @NotBlank String idempotencyToken
) {}
