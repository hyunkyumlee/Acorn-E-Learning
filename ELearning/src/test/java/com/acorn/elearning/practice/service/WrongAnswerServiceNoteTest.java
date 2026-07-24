package com.acorn.elearning.practice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.mapper.ScoreEventMapper;
import com.acorn.elearning.practice.mapper.WrongAnswerMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.WrongAnswer;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.security.SessionUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WrongAnswerServiceNoteTest {

    @Test
    void note_builds_downloadable_markdown_from_owned_wrong_answer() {
        WrongAnswerMapper wrongAnswerMapper = mock(WrongAnswerMapper.class);
        PracticeProblemMapper practiceProblemMapper = mock(PracticeProblemMapper.class);
        WrongAnswerService service = new WrongAnswerService(
                wrongAnswerMapper,
                practiceProblemMapper,
                mock(PracticeSubmissionMapper.class),
                mock(ScoreEventMapper.class),
                mock(ScoreService.class)
        );

        WrongAnswer wrongAnswer = new WrongAnswer();
        wrongAnswer.setWrongAnswerId(31L);
        wrongAnswer.setUserId(7L);
        wrongAnswer.setProblemId(19L);
        wrongAnswer.setWrongCount(2);
        wrongAnswer.setReviewStatus("PENDING");

        PracticeProblem problem = new PracticeProblem();
        problem.setProblemId(19L);
        problem.setSubjectId(2L);
        problem.setQuestion("list의 마지막 값을 꺼내려면 어떤 인덱스를 사용하나요?");
        problem.setAnswerText("-1");
        problem.setExplanation("음수 인덱스 -1은 마지막 요소를 가리킵니다.");

        when(wrongAnswerMapper.findByIdWrongAnswer(31L)).thenReturn(Optional.of(wrongAnswer));
        when(practiceProblemMapper.findById(19L)).thenReturn(Optional.of(problem));

        WrongAnswerNote note = service.note(sessionUser(), 31L);

        assertEquals("knowva-wrong-note-31.md", note.fileName());
        assertEquals(2L, note.subjectId());
        assertEquals("오답 복습 | list의 마지막 값을 꺼내려면 어떤 인덱스를 사용하나요?", note.postTitle());
        assertTrue(note.markdown().contains("## 문제\n> list의 마지막 값을 꺼내려면 어떤 인덱스를 사용하나요?"));
        assertTrue(note.markdown().contains("## 정답\n> -1"));
        assertTrue(note.markdown().contains("## 해설\n> 음수 인덱스 -1은 마지막 요소를 가리킵니다."));
    }

    private SessionUser sessionUser() {
        return new SessionUser(7L, "learner@example.com", "학습자", SessionUser.ROLE_USER, false);
    }
}
