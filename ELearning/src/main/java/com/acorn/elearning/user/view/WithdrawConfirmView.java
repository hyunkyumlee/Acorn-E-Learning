package com.acorn.elearning.user.view;

import java.util.Map;

public record WithdrawConfirmView(String title, String status, Map<String, Object> attributes) {
    public static WithdrawConfirmView stub(String title) { return new WithdrawConfirmView(title, "SKELETON", Map.of()); }
}
