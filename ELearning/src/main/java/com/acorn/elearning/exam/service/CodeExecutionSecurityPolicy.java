package com.acorn.elearning.exam.service;

import java.util.List;
import java.util.Locale;

final class CodeExecutionSecurityPolicy {
    private static final int MAX_SOURCE_LENGTH = 20_000;
    private static final List<String> BLOCKED_TOKENS = List.of(
            "package ",
            "java.io",
            "java.nio",
            "java.net",
            "java.lang.reflect",
            "javax.",
            "sun.",
            "com.sun.",
            "runtime.getruntime",
            "processbuilder",
            "system.exit",
            "system.getenv",
            "system.getproperty",
            "classloader",
            "class.forname",
            "thread",
            "executor",
            "socket",
            "serverSocket",
            "fileinputstream",
            "fileoutputstream",
            "randomaccessfile");

    String violationMessage(String source) {
        if (source == null || source.isBlank()) {
            return "실행할 코드가 비어 있습니다.";
        }
        if (source.length() > MAX_SOURCE_LENGTH) {
            return "제출 코드 길이가 허용 범위를 초과했습니다.";
        }
        String normalizedSource = source.toLowerCase(Locale.ROOT);
        for (String token : BLOCKED_TOKENS) {
            if (normalizedSource.contains(token.toLowerCase(Locale.ROOT))) {
                return "허용되지 않는 API 또는 패키지가 포함되어 코드 실행을 차단했습니다.";
            }
        }
        return "";
    }
}
