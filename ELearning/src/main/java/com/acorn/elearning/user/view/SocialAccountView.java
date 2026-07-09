package com.acorn.elearning.user.view;

import com.acorn.elearning.auth.dto.response.SocialAccountListResponse;
import com.acorn.elearning.auth.dto.response.SocialAccountResponse;
import com.acorn.elearning.user.dto.response.UserProfileResponse;

public record SocialAccountView(
        String title,
        UserProfileResponse profile,
        SocialAccountListResponse socialAccounts
) {
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
                .filter(account -> normalizedProvider.equals(account.provider()))
                .findFirst()
                .orElse(null);
    }
}
