package com.acorn.elearning.security;

import java.io.Serializable;

public record SessionUser(Long userId, String email, String nickname, String role, boolean premiumActive, String profileImageUrl) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "LOGIN_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public SessionUser(Long userId, String email, String nickname, String role, boolean premiumActive) {
        this(userId, email, nickname, role, premiumActive, null);
    }

    public boolean admin() { return ROLE_ADMIN.equals(role); }
    public boolean user() { return ROLE_USER.equals(role); }

    public String defaultRedirectPath() {
        return admin() ? "/admin" : "/learning";
    }
}
