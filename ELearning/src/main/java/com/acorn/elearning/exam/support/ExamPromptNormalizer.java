package com.acorn.elearning.exam.support;

public final class ExamPromptNormalizer {
    private ExamPromptNormalizer() {}

    public static String normalize(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return prompt;
        }
        return prompt
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\r", "\r");
    }
}
