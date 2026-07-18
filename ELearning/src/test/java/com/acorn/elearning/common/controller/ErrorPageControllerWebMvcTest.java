package com.acorn.elearning.common.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ErrorPageControllerWebMvcTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ErrorPageController()).build();

    @Test
    void healthEndpointReturnsOkWithoutRenderingView() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void directErrorRoutesRenderTheirStatusSpecificViews() throws Exception {
        mockMvc.perform(get("/error/403"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        mockMvc.perform(get("/error/404"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        mockMvc.perform(get("/error/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"));
    }
}
