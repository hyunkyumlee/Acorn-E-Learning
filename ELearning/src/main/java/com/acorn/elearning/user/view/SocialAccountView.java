package com.acorn.elearning.user.view;

import com.acorn.elearning.auth.dto.response.SocialAccountListResponse;
import com.acorn.elearning.auth.dto.response.SocialAccountResponse;
import com.acorn.elearning.user.dto.response.UserProfileResponse;

public record SocialAccountView(
        String title,
        UserProfileResponse profile,
        SocialAccountListResponse socialAccounts,
        LoginAccount loginAccount
) {
    public record LoginAccount(
            String provider,
            String providerLabel,
            String email
    ) {
        public static LoginAccount email(String email) {
            return new LoginAccount("email", "이메일", email);
        }

        public static LoginAccount social(String provider, String email) {
            return new LoginAccount(normalizeProvider(provider), providerLabel(provider), email);
        }

        public static LoginAccount unknown(String email) {
            return new LoginAccount("unknown", "확인 필요", email);
        }

        private static String providerLabel(String provider) {
            return switch (normalizeProvider(provider)) {
                case "google" -> "Google";
                case "github" -> "GitHub";
                case "email" -> "이메일";
                default -> "확인 필요";
            };
        }

        private static String normalizeProvider(String provider) {
            return provider == null ? "unknown" : provider.trim().toLowerCase();
        }
    }

    public String loginProviderLabel() {
        return loginAccount == null ? "확인 필요" : loginAccount.providerLabel();
    }

    public String loginEmail() {
        if (loginAccount == null || loginAccount.email() == null || loginAccount.email().isBlank()) {
            return "-";
        }
        return loginAccount.email();
    }

    public String accountLabel(String provider) {
        SocialAccountResponse account = activeAccount(provider);
        if (account == null || account.providerEmail() == null || account.providerEmail().isBlank()) {
            return "연동되지 않음";
        }
        return account.providerEmail();
    }

    private SocialAccountResponse activeAccount(String provider) {
        if (provider == null || socialAccounts == null || socialAccounts.accounts() == null) {
            return null;
        }
        String normalizedProvider = provider.trim().toLowerCase();
        return socialAccounts.accounts().stream()
                .filter(SocialAccountResponse::active)
                .filter(account -> normalizedProvider.equals(normalizeProvider(account.provider())))
                .findFirst()
                .orElse(null);
    }

    private String normalizeProvider(String provider) {
        return provider == null ? "" : provider.trim().toLowerCase();
    }
}
