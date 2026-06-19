package com.acorn.elearning.payment.view;

import java.util.Map;

public record PaymentResultView(String title, String status, Map<String, Object> attributes) {
    public static PaymentResultView stub(String title) { return new PaymentResultView(title, "SKELETON", Map.of()); }
}
