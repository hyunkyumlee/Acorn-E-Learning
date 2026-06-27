package com.acorn.elearning.common.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    @Test
    void messages_use_polite_korean_sentence_endings() {
        boolean allPolite = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::message)
                .allMatch(message -> message.endsWith("니다.") || message.endsWith("세요."));

        assertTrue(allPolite);
        assertFalse(Arrays.stream(ErrorCode.values()).map(ErrorCode::message).anyMatch(message -> message.endsWith("해")));
    }
}
