package com.acorn.elearning.common.ai;

import java.util.List;
import java.util.regex.Pattern;

public final class AiGeneratedTextSanitizer {
    private static final Pattern SENTENCE_BREAK = Pattern.compile("(?<=[.!?。])\\s+|\\R+");
    private static final List<String> STARTER_CODE_TERMS = List.of(
            "starter",
            "starterCode",
            "Scanner",
            "import",
            "class",
            "main",
            "TODO",
            "기본 구조",
            "입력 처리",
            "입력값을 읽",
            "입력을 받",
            "변수를 선언",
            "변수와 배열을 선언",
            "기본적인 메서드 작성",
            "메서드 작성",
            "배열을 선언",
            "배열 선언",
            "배열에 저장",
            "sc.close");
    private static final List<String> PRAISE_TERMS = List.of(
            "좋은 시작",
            "좋은 점",
            "강점",
            "올바르게",
            "올바릅니다",
            "잘 작성",
            "잘 하",
            "정상",
            "갖추",
            "가능성",
            "보여주",
            "대체로",
            "적절");
    private static final List<String> GENERIC_STARTER_PRAISE_TERMS = List.of(
            "좋은 시작입니다",
            "좋은 점은 있습니다",
            "기본은 좋습니다",
            "기본기는 좋습니다");
    private static final List<String> HIDDEN_DEVELOPMENT_TERMS = List.of(
            "TODO",
            "구현 예시");

    private AiGeneratedTextSanitizer() {
    }

    public static String removeStarterCodePraise(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String sanitized = SENTENCE_BREAK.splitAsStream(text.strip())
                .map(String::strip)
                .filter(sentence -> !sentence.isBlank())
                .filter(sentence -> !isStarterCodePraise(sentence))
                .reduce("", AiGeneratedTextSanitizer::joinSentence);
        return sanitized.strip();
    }

    public static String cleanUserFacingAiText(String text) {
        String sanitized = removeStarterCodePraise(text);
        if (sanitized.isBlank()) {
            return "";
        }
        return SENTENCE_BREAK.splitAsStream(sanitized)
                .map(String::strip)
                .map(AiGeneratedTextSanitizer::replaceUserFacingTerms)
                .filter(sentence -> !sentence.isBlank())
                .filter(sentence -> !containsAny(sentence, HIDDEN_DEVELOPMENT_TERMS))
                .reduce("", AiGeneratedTextSanitizer::joinSentence)
                .strip();
    }

    public static boolean isStarterCodePraise(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.strip();
        return containsAny(trimmed, GENERIC_STARTER_PRAISE_TERMS)
                || (containsAny(trimmed, PRAISE_TERMS) && containsAny(trimmed, STARTER_CODE_TERMS));
    }

    private static boolean containsAny(String text, List<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }

    private static String joinSentence(String left, String right) {
        if (left.isBlank()) {
            return right;
        }
        return left + " " + right;
    }

    private static String replaceUserFacingTerms(String text) {
        return text
                .replace("문제 요구사항", "문제 조건")
                .replace("요구사항", "조건")
                .strip();
    }
}
