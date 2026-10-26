package com.example.WordGame.modules.auth.controller;

import com.example.WordGame.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();

    @Test
    void resetPasswordLinkShouldRenderBrowserForm() throws Exception {
        mockMvc.perform(get("/api/auth/reset-password").param("token", "reset-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reset your password")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("new URLSearchParams(window.location.search)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method: 'POST'")));
    }
}
