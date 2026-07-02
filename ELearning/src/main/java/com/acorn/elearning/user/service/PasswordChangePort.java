package com.acorn.elearning.user.service;

public interface PasswordChangePort {
    void changePassword(Long userId, String currentPassword, String newPassword);
}
