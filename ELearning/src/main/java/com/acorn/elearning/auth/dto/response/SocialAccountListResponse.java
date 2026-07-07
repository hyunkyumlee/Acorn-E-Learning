package com.acorn.elearning.auth.dto.response;

import com.acorn.elearning.auth.model.SocialAccount;

import java.util.List;
import java.util.Map;

public record SocialAccountListResponse(List<SocialAccountResponse> accounts) {
    public static SocialAccountListResponse from(List<SocialAccount> rows) {
        return new SocialAccountListResponse(
                rows.stream().map(SocialAccountResponse::from).toList()
        );
    }
}
