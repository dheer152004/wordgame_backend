package com.example.WordGame.Config;

import com.example.WordGame.modules.auth.service.JwtAuthFilter;
import com.example.WordGame.modules.legalDocuments.controller.LegalDocumentController;
import com.example.WordGame.modules.legalDocuments.service.LegalDocumentService;
import com.example.WordGame.modules.roles.user.profile.ProfileController;
import com.example.WordGame.modules.roles.user.profile.ProfileService;
import com.example.WordGame.Service.TestController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@WebMvcTest(controllers = {LegalDocumentController.class, TestController.class, ProfileController.class})
@Import(WebSecurityConfig.class)
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private LegalDocumentService legalDocumentService;

    @MockBean
    private ProfileService profileService;

    @BeforeEach
    void passRequestsThroughMockFilters() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            if ("alice".equals(request.getHeader("X-Test-User"))) {
                UserDetails user = User.withUsername("alice").password("").roles("USER").build();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            }
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitingFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void patchLegalDocumentStatusShouldBeAllowedWithoutAuthentication() throws Exception {
        mockMvc.perform(patch("/api/legal-documents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                    .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testHelloShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/test/hello"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"message":"Hello from Nroq Backend!","status":"Running"}
                        """));
    }

    @Test
    void deleteProfileShouldBeAllowedForAuthenticatedUserWithoutRequestBody() throws Exception {
        mockMvc.perform(delete("/api/user/profile").header("X-Test-User", "alice"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"message":"Profile deleted successfully"}
                        """));

        verify(profileService).deleteProfile("alice", null);
    }

    @Test
    void deleteProfileShouldRequireAuthentication() throws Exception {
        mockMvc.perform(delete("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }
}
