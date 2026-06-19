package com.acorn.elearning.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreateDummyPaymentRequest(@NotBlank String requestId, Map<String, Object> payload) {}
