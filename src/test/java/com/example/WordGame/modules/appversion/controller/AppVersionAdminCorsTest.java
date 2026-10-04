package com.example.WordGame.modules.appversion.controller;

import com.example.WordGame.Config.RateLimitingFilter;
import com.example.WordGame.Config.WebSecurityConfig;
import com.example.WordGame.modules.appversion.service.AppVersionPolicyService;
import com.example.WordGame.modules.auth.service.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAppVersionController.class)
@Import(WebSecurityConfig.class)
class AppVersionAdminCorsTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private AppVersionPolicyService service;

    @Test
    void allowsAdminPanelPreflightFromLocalReactOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/admin/app-versions")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}