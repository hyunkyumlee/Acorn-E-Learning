package com.acorn.elearning.practice.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.acorn.elearning.common.exception.GlobalExceptionHandler;
import com.acorn.elearning.exam.service.TestCaseExecutionService;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.practice.service.FreeCodingService;
import com.acorn.elearning.practice.service.PracticeService;
import com.acorn.elearning.practice.service.ProblemService;
import com.acorn.elearning.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PracticeControllerFreeCodingWebMvcTest {

    @Test
    void freeCoding_rejects_admin_before_rendering_the_workbench() throws Exception {
        mockMvc().perform(get("/practice/free-coding")
                        .session(sessionUser(SessionUser.ROLE_ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH-FORBIDDEN"));
    }

    @Test
    void freeCoding_renders_the_workbench_for_a_learner() throws Exception {
        mockMvc().perform(get("/practice/free-coding")
                        .session(sessionUser(SessionUser.ROLE_USER))
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("practice/free-coding"));
    }

    private MockMvc mockMvc() {
        PracticeController controller = new PracticeController(
                mock(ProblemService.class),
                mock(PracticeService.class),
                new FreeCodingService(mock(TestCaseExecutionService.class)),
                mock(LessonMapper.class),
                mock(CurriculumNodeMapper.class));
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private MockHttpSession sessionUser(String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(1L, "user@example.com", "학습자", role, false));
        return session;
    }
}
