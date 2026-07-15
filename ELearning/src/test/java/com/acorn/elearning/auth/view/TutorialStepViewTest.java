package com.acorn.elearning.auth.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TutorialStepViewTest {

    @Test
    void threeArgFactory_leavesNuviFieldsNull() {
        TutorialStepView step = TutorialStepView.of(1, "제목", "설명");

        assertNull(step.imageUrl());
        assertNull(step.nuviXPercent());
        assertNull(step.nuviYPercent());
        assertNull(step.nuviPose());
        assertNull(step.highlightLeftPercent());
        assertNull(step.highlightTopPercent());
        assertNull(step.highlightWidthPercent());
        assertNull(step.highlightHeightPercent());
        assertNull(step.bubbleText());
    }

    @Test
    void fullFactory_setsNuviCoordinatesPoseHighlightAndBubble() {
        TutorialStepView step = TutorialStepView.of(
                1, "로드맵 기반 학습", "설명", "/img/tut1.png", 88, 30, "WAVING",
                20, 21, 58, 71, "학습 진행도를 한 눈에 볼 수 있어요!");

        assertEquals(88, step.nuviXPercent());
        assertEquals(30, step.nuviYPercent());
        assertEquals("WAVING", step.nuviPose());
        assertEquals(20, step.highlightLeftPercent());
        assertEquals(21, step.highlightTopPercent());
        assertEquals(58, step.highlightWidthPercent());
        assertEquals(71, step.highlightHeightPercent());
        assertEquals("학습 진행도를 한 눈에 볼 수 있어요!", step.bubbleText());
    }
}
