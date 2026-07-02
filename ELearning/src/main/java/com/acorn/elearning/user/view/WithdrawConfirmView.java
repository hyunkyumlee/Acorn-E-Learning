package com.acorn.elearning.user.view;

import com.acorn.elearning.user.dto.response.UserProfileResponse;

public record WithdrawConfirmView(
        String title,
        UserProfileResponse profile
) {}
