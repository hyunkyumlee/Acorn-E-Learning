package com.acorn.elearning.user.view;

import com.acorn.elearning.user.dto.response.UserProfileResponse;

public record SocialAccountView(
        String title,
        UserProfileResponse profile
) {
    public String googleAccountLabel() {
        return profile.email() + " 계정이 연결되어 있습니다.";
    }
}
