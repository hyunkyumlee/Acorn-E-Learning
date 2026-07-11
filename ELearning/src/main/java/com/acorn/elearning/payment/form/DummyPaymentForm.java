package com.acorn.elearning.payment.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DummyPaymentForm {
    @NotBlank
    private String productCode = "PREMIUM_LIFETIME";

    @NotBlank
    @Pattern(regexp = "CARD|BANK_TRANSFER|KAKAO_PAY", message = "결제 방식은 CARD, BANK_TRANSFER, KAKAO_PAY만 가능합니다.")
    private String paymentMethod;

    @Size(max = 4)
    private String cardLast4;

    @Size(max = 100)
    private String depositorName;

    @Size(max = 255)
    private String memo;

    @NotBlank
    private String idempotencyToken;
}
