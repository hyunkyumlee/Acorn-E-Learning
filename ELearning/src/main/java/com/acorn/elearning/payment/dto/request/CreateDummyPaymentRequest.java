package com.acorn.elearning.payment.dto.request;

import com.acorn.elearning.payment.form.DummyPaymentForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDummyPaymentRequest(
        @NotBlank String productCode,
        @NotBlank @Pattern(regexp = "CARD|BANK_TRANSFER") String paymentMethod,
        @Size(max = 4) String cardLast4,
        @Size(max = 100) String depositorName,
        @Size(max = 255) String memo,
        @NotBlank String idempotencyToken
) {
    public DummyPaymentForm toForm() {
        DummyPaymentForm form = new DummyPaymentForm();
        form.setProductCode(productCode);
        form.setPaymentMethod(paymentMethod);
        form.setCardLast4(cardLast4);
        form.setDepositorName(depositorName);
        form.setMemo(memo);
        form.setIdempotencyToken(idempotencyToken);
        return form;
    }
}
