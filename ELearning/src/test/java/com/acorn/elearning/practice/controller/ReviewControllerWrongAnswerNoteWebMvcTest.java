package com.acorn.elearning.practice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acorn.elearning.common.exception.GlobalExceptionHandler;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.practice.service.WrongAnswerService;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReviewControllerWrongAnswerNoteWebMvcTest {

    @Test
    void downloadNote_returns_utf8_markdown_attachment() throws Exception {
        WrongAnswerNote note = note();
        MockMvc mockMvc = mockMvc(note);

        mockMvc.perform(get("/learning/review/31/note.md").session(sessionUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("knowva-wrong-note-31.md")))
                .andExpect(header().longValue("Content-Length", note.markdown().getBytes(java.nio.charset.StandardCharsets.UTF_8).length))
                .andExpect(content().string(note.markdown()));
    }

    @Test
    void createCommunityDraft_redirects_with_editable_note_flash_attribute() throws Exception {
        WrongAnswerNote note = note();
        MockMvc mockMvc = mockMvc(note);

        mockMvc.perform(post("/learning/review/31/community-draft").session(sessionUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/write"))
                .andExpect(flash().attribute("wrongAnswerCommunityDraft", note));
    }

    private MockMvc mockMvc(WrongAnswerNote note) {
        WrongAnswerService wrongAnswerService = mock(WrongAnswerService.class);
        when(wrongAnswerService.note(any(SessionUser.class), eq(31L))).thenReturn(note);

        ReviewController controller = new ReviewController(
                wrongAnswerService,
                mock(LessonMapper.class),
                mock(CurriculumNodeMapper.class)
        );
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private WrongAnswerNote note() {
        return new WrongAnswerNote(
                31L,
                2L,
                "knowva-wrong-note-31.md",
                "오답 복습 | 인덱스",
                "# 오답 복습 | 인덱스\n"
        );
    }

    private MockHttpSession sessionUser() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(7L, "learner@example.com", "학습자", SessionUser.ROLE_USER, false));
        return session;
    }
}
