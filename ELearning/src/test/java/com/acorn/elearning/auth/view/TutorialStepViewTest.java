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
    }

    @Test
    void fullFactory_setsNuviCoordinatesAndPose() {
        TutorialStepView step = TutorialStepView.of(
                1, "로드맵 기반 학습", "설명", "/img/tut1.png", 62, 30, "EXPLORING");

        assertEquals(62, step.nuviXPercent());
        assertEquals(30, step.nuviYPercent());
        assertEquals("EXPLORING", step.nuviPose());
    }
}
