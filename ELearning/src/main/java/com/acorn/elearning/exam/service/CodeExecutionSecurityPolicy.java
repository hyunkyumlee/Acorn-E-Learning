package com.acorn.elearning.exam.service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeExecutionSecurityPolicy {
    private static final int MAX_SOURCE_LENGTH = 20_000;
    private static final String BLOCKED_MESSAGE = "허용되지 않는 API 또는 패키지가 포함되어 코드 실행을 차단했습니다.";
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("\\\\u[0-9a-fA-F]{4}");
    private static final Pattern PACKAGE_DECLARATION = Pattern.compile("\\bpackage\\b");
    private static final Pattern IMPORT_DECLARATION = Pattern.compile("\\bimport\\s+(?:static\\s+)?([^;]+);");
    private static final Set<String> ALLOWED_IMPORTS = Set.of(
            "java.util.*",
            "java.util.Scanner",
            "java.util.Arrays",
            "java.util.ArrayList",
            "java.util.Collections",
            "java.util.Comparator",
            "java.util.HashMap",
            "java.util.HashSet",
            "java.util.List",
            "java.util.Map",
            "java.util.Set",
            "java.util.StringTokenizer");
    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("\\bjava\\s*\\.\\s*(?:io|nio|net|lang\\s*\\.\\s*reflect)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bjavax\\s*\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bsun\\s*\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bcom\\s*\\.\\s*sun\\s*\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(?:Runtime|Process|ProcessBuilder|ProcessHandle|Class|ClassLoader|Thread|Executor|Socket|ServerSocket|File|Files|Path|Paths|URL|URI|RandomAccessFile|FileInputStream|FileOutputStream)\\b"),
            Pattern.compile("\\bSystem\\s*\\.\\s*(?!(?:out\\s*\\.\\s*(?:print|println|printf)\\s*\\()|(?:in\\b))"),
            Pattern.compile("\\b(?:exec|getenv|getProperties|getProperty|load|loadLibrary|setSecurityManager)\\s*\\("),
            Pattern.compile("\\.\\s*getClass\\s*\\("));

    String violationMessage(String source) {
        if (source == null || source.isBlank()) {
            return "실행할 코드가 비어 있습니다.";
        }
        if (source.length() > MAX_SOURCE_LENGTH) {
            return "제출 코드 길이가 허용 범위를 초과했습니다.";
        }
        if (UNICODE_ESCAPE.matcher(source).find()) {
            return BLOCKED_MESSAGE;
        }

        String policySource = removeCommentsAndLiterals(source);
        if (PACKAGE_DECLARATION.matcher(policySource).find() || containsBlockedImport(policySource)) {
            return BLOCKED_MESSAGE;
        }
        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(policySource).find()) {
                return BLOCKED_MESSAGE;
            }
        }
        return "";
    }

    private boolean containsBlockedImport(String source) {
        Matcher matcher = IMPORT_DECLARATION.matcher(source);
        while (matcher.find()) {
            String importedName = matcher.group(1).replaceAll("\\s+", "");
            if (!ALLOWED_IMPORTS.contains(importedName)) {
                return true;
            }
        }
        return false;
    }

    private String removeCommentsAndLiterals(String source) {
        StringBuilder result = new StringBuilder(source.length());
        boolean lineComment = false;
        boolean blockComment = false;
        boolean stringLiteral = false;
        boolean charLiteral = false;
        boolean escaped = false;

        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';

            if (lineComment) {
                if (current == '\n') {
                    lineComment = false;
                    result.append(current);
                } else {
                    result.append(' ');
                }
                continue;
            }

            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    result.append("  ");
                    index++;
                } else {
                    result.append(current == '\n' ? current : ' ');
                }
                continue;
            }

            if (stringLiteral || charLiteral) {
                result.append(current == '\n' ? current : ' ');
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (current == '\\') {
                    escaped = true;
                    continue;
                }
                if ((stringLiteral && current == '"') || (charLiteral && current == '\'')) {
                    stringLiteral = false;
                    charLiteral = false;
                }
                continue;
            }

            if (current == '/' && next == '/') {
                lineComment = true;
                result.append("  ");
                index++;
            } else if (current == '/' && next == '*') {
                blockComment = true;
                result.append("  ");
                index++;
            } else if (current == '"') {
                stringLiteral = true;
                result.append(' ');
            } else if (current == '\'') {
                charLiteral = true;
                result.append(' ');
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }
}
