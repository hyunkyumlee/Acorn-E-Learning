package com.acorn.elearning.common.validation;

import java.util.regex.Pattern;

// 비밀번호 정책 : 8~16자, 영문 대소문자+숫자+특수문자 전부 포함, 닉네임/이메일 아이디와 중복 금지.
// @StrongPassword(같은 DTO 안에 닉네임/이메일이 있는 경우)와 PasswordResetService/SettingsService(DTO에 없는 경우)
// 양쪽에서 같은 규칙을 쓰도록 로직을 여기 하나로 모음.
public final class PasswordPolicy {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 16;
    // 닉네임/이메일 아이디가 너무 짧으면(1자) 우연히 겹치는 경우가 흔해서 의미 있는 길이부터만 검사
    private static final int MIN_PROFILE_MATCH_LENGTH = 2;

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private PasswordPolicy() {}

    public static boolean isStrong(String password) {
        if (password == null) return false;
        int length = password.length();
        return length >= MIN_LENGTH && length <= MAX_LENGTH
                && UPPER.matcher(password).find()
                && LOWER.matcher(password).find()
                && DIGIT.matcher(password).find()
                && SPECIAL.matcher(password).find();
    }

    // 비밀번호가 닉네임 또는 이메일의 @ 앞부분을 포함하면 true (대소문자 무시)
    public static boolean containsProfileInfo(String password, String nickname, String email) {
        if (password == null || password.isBlank()) return false;
        String lowerPassword = password.toLowerCase();

        if (nickname != null && nickname.length() >= MIN_PROFILE_MATCH_LENGTH
                && lowerPassword.contains(nickname.toLowerCase())) {
            return true;
        }

        String localPart = emailLocalPart(email);
        return localPart != null && localPart.length() >= MIN_PROFILE_MATCH_LENGTH
                && lowerPassword.contains(localPart.toLowerCase());
    }

    private static String emailLocalPart(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : null;
    }
}
