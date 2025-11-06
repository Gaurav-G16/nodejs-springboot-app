package com.example.userapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = SystemDashboardController.class)
class SystemDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Environment environment;

    @Test
    void testDashboard() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("apiEndpoints"))
                .andExpect(model().attributeExists("appInfo"))
                .andExpect(model().attributeExists("systemStats"))
                .andExpect(model().attributeExists("healthStatus"))
                .andExpect(model().attributeExists("dbConnected"));
    }
}
