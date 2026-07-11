package com.acorn.elearning.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

class GlobalExceptionHandlerWebMvcTest {

    @Test
    void htmlRequestsForUnexpectedErrorsRenderTheServerErrorPage() throws Exception {
        mockMvc().perform(get("/test-errors/unexpected").accept(MediaType.TEXT_HTML))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"));
    }

    @Test
    void jsonRequestsKeepTheApiResponseWhenHtmlIsRejected() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/test-errors/unexpected")
                        .header(HttpHeaders.ACCEPT, "application/json, text/html;q=0"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("COMMON-500"));

        mockMvc.perform(get("/test-errors/unexpected")
                        .header(HttpHeaders.ACCEPT, "text/html;q=0, application/json;q=1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("COMMON-500"));

        mockMvc.perform(get("/test-errors/unexpected")
                        .header(HttpHeaders.ACCEPT, "text/html;q=0"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("COMMON-500"));
    }

    @Test
    void equalQualityAcceptHeadersFollowTheClientPreferenceOrder() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/test-errors/unexpected")
                        .header(HttpHeaders.ACCEPT, "application/json, text/html"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("COMMON-500"));

        mockMvc.perform(get("/test-errors/unexpected")
                        .header(HttpHeaders.ACCEPT, "text/html, application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"));
    }

    @Test
    void businessErrorsKeepTheApiResponseWhenHtmlIsRejected() throws Exception {
        mockMvc().perform(get("/test-errors/auth-required")
                        .header(HttpHeaders.ACCEPT, "text/html;q=0"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("AUTH-401"));
    }

    @Test
    void bindingFailuresDoNotExposeFrameworkConversionDetails() {
        BindException exception = new BindException(new Object(), "request");
        exception.addError(new FieldError(
                "request",
                "page",
                "abc",
                true,
                null,
                null,
                "Failed to convert property value of type 'java.lang.String' to required type 'int'"));

        var response = new GlobalExceptionHandler().handleBindException(exception);

        assertEquals("입력값 형식이 올바르지 않습니다.", response.getBody().error().fieldErrors().get(0).message());
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new FailingPageController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Controller
    static class FailingPageController {

        @GetMapping("/test-errors/unexpected")
        String unexpectedError() {
            throw new IllegalStateException("test failure");
        }

        @GetMapping("/test-errors/auth-required")
        String authRequired() {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }
}
