package com.acorn.elearning.payment.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayReadyResponse(
        String tid,
        @JsonProperty("next_redirect_app_url") String nextRedirectAppUrl,
        @JsonProperty("next_redirect_mobile_url") String nextRedirectMobileUrl,
        @JsonProperty("next_redirect_pc_url") String nextRedirectPcUrl,
        @JsonProperty("android_app_scheme") String androidAppScheme,
        @JsonProperty("ios_app_scheme") String iosAppScheme,
        @JsonProperty("created_at") String createdAt
) {
    public String redirectUrl() {
        if (hasText(nextRedirectPcUrl)) {
            return nextRedirectPcUrl;
        }
        if (hasText(nextRedirectMobileUrl)) {
            return nextRedirectMobileUrl;
        }
        return nextRedirectAppUrl;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
