package com.acorn.elearning.security;

public record SessionUser(Long userId, String email, String nickname, String role, boolean premiumActive) {
    public static final String SESSION_KEY = "LOGIN_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public boolean admin() { return ROLE_ADMIN.equals(role); }

    public String defaultRedirectPath() {
        return admin() ? "/admin" : "/learning";
    }
}
