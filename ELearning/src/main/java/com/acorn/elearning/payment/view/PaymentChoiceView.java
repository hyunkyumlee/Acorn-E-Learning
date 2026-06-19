package com.acorn.elearning.payment.view;

import java.util.Map;

public record PaymentChoiceView(String title, String status, Map<String, Object> attributes) {
    public static PaymentChoiceView stub(String title) { return new PaymentChoiceView(title, "SKELETON", Map.of()); }
}
