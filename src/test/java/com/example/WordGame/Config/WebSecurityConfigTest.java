package com.example.WordGame.Config;

import com.example.WordGame.modules.auth.service.JwtAuthFilter;
import com.example.WordGame.modules.legalDocuments.controller.LegalDocumentController;
import com.example.WordGame.modules.legalDocuments.service.LegalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LegalDocumentController.class)
@Import(WebSecurityConfig.class)
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private LegalDocumentService legalDocumentService;

    @Test
    void patchLegalDocumentStatusShouldBeAllowedWithoutAuthentication() throws Exception {
        mockMvc.perform(patch("/api/legal-documents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().is4xxClientError());
    }
}
