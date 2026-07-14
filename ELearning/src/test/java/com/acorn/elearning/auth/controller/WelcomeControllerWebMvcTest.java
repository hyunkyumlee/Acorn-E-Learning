package com.acorn.elearning.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.acorn.elearning.auth.view.TutorialStepView;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

class WelcomeControllerWebMvcTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WelcomeController()).build();

    @Test
    void anonymousWelcome_exposesFiveTutorialStepsWithNuviCoordinates() throws Exception {
        ModelAndView mav = mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome/index"))
                .andExpect(model().attributeExists("tutorialSteps"))
                .andReturn()
                .getModelAndView();

        @SuppressWarnings("unchecked")
        List<TutorialStepView> steps = (List<TutorialStepView>) mav.getModel().get("tutorialSteps");

        assertEquals(5, steps.size());
        for (TutorialStepView step : steps) {
            assertNotNull(step.nuviXPercent(), "step " + step.step());
            assertNotNull(step.nuviYPercent(), "step " + step.step());
            assertNotNull(step.nuviPose(), "step " + step.step());
        }
    }
}
