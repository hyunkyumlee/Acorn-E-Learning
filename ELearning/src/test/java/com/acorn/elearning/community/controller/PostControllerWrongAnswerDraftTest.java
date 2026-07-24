package com.acorn.elearning.community.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.acorn.elearning.common.idempotency.IdempotencyTokenService;
import com.acorn.elearning.community.form.PostForm;
import com.acorn.elearning.community.mapper.CommunityNoticeMapper;
import com.acorn.elearning.community.mapper.CommunityWriterMapper;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.mock.web.MockHttpSession;

class PostControllerWrongAnswerDraftTest {

    @Test
    void createForm_prefills_editable_community_draft_from_wrong_answer_note() {
        PostController controller = new PostController(
                mock(PostService.class),
                mock(CommunityWriterMapper.class),
                mock(CommunityNoticeMapper.class),
                new IdempotencyTokenService()
        );
        WrongAnswerNote note = new WrongAnswerNote(
                31L,
                2L,
                "knowva-wrong-note-31.md",
                "오답 복습 | 인덱스",
                "# 오답 복습 | 인덱스\n\n## 문제\n> 마지막 값"
        );
        ExtendedModelMap model = new ExtendedModelMap();
        model.addAttribute("wrongAnswerCommunityDraft", note);

        String viewName = controller.createForm(sessionUser(), model, new MockHttpSession());
        PostForm form = (PostForm) model.getAttribute("form");

        assertEquals("community/write", viewName);
        assertNotNull(form);
        assertEquals(note.postTitle(), form.getTitle());
        assertEquals(note.markdown(), form.getContent());
        assertEquals(2L, form.getSubjectId());
        assertEquals("STUDY_LOG", form.getBoardType());
        assertNotNull(form.getIdempotencyToken());
        assertEquals("오답노트 내용을 불러왔습니다. 수정한 뒤 등록해 주세요.", model.getAttribute("communityDraftNotice"));
    }

    private SessionUser sessionUser() {
        return new SessionUser(7L, "learner@example.com", "학습자", SessionUser.ROLE_USER, false);
    }
}
