package com.acorn.elearning.payment.view;

import java.util.Map;

public record PaymentHistoryView(String title, String status, Map<String, Object> attributes) {
    public static PaymentHistoryView stub(String title) { return new PaymentHistoryView(title, "SKELETON", Map.of()); }
}
