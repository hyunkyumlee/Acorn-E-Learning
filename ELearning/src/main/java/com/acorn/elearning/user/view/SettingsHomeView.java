package com.acorn.elearning.user.view;

import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.dto.response.UserSettingsResponse;

public record SettingsHomeView(
        String title,
        UserProfileResponse profile,
        UserSettingsResponse settings
) {}
