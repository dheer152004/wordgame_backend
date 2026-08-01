package com.example.WordGame.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingFilterTest {

    @Test
    void shouldBlockRequestsAfterHundredPerMinuteForSameIp() throws ServletException, IOException {
        RateLimitingFilter filter = new RateLimitingFilter();

        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.setRemoteAddr("203.0.113.10");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new FilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
                    // no-op
                }
            });

            assertEquals(200, response.getStatus(), "Request " + i + " should be allowed");
        }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("GET", "/api/test");
        blockedRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        filter.doFilter(blockedRequest, blockedResponse, (request, response) -> {
            // no-op
        });

        assertEquals(429, blockedResponse.getStatus());
        assertTrue(blockedResponse.getContentAsString().contains("Too many requests"));
    }
}
