package com.acorn.elearning.common.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void success_uses_polite_default_message_when_data_is_returned() {
        ApiResponse<String> response = ApiResponse.success("payload");

        assertTrue(response.success());
        assertEquals("요청이 정상적으로 처리되었습니다.", response.message());
        assertEquals("payload", response.data());
        assertNull(response.error());
    }

    @Test
    void fail_keeps_field_errors_when_validation_fails() {
        List<ApiResponse.FieldError> fieldErrors = List.of(new ApiResponse.FieldError("email", "이메일 형식이 올바르지 않습니다."));

        ApiResponse<Void> response = ApiResponse.fail(
                "입력값이 올바르지 않습니다.",
                "VALIDATION-400",
                "요청 본문 검증에 실패했습니다.",
                fieldErrors);

        assertFalse(response.success());
        assertEquals("입력값이 올바르지 않습니다.", response.message());
        assertEquals("VALIDATION-400", response.error().code());
        assertEquals(fieldErrors, response.error().fieldErrors());
    }
}
