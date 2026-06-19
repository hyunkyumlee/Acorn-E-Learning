package com.acorn.elearning.payment.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DummyPaymentForm {
    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;
}
