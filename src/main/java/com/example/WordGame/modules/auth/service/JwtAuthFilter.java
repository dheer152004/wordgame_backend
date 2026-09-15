package com.example.WordGame.modules.auth.service;

import com.example.WordGame.modules.auth.repository.AuthUtil;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("incoming request: {}", request.getRequestURI());

            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader == null) {
                log.debug("no Authorization header, allowing anonymous request");
                filterChain.doFilter(request, response);
                return;
            }

            String headerLower = requestTokenHeader.toLowerCase();
            String token = null;
            if (headerLower.startsWith("bearer ")) {
                token = requestTokenHeader.substring(7).trim();
            } else if (headerLower.startsWith("bearer")) {
                // allow missing space after Bearer
                token = requestTokenHeader.substring(6).trim();
            } else {
                // not a bearer token, proceed without authenticating
                filterChain.doFilter(request, response);
                return;
            }

            if (token == null || token.isBlank()) {
                // token missing after Bearer
                log.warn("Authorization header contained Bearer but no token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or empty Bearer token");
                return;
            }
            if (!authUtil.isAccessToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token cannot authenticate API requests");
                return;
            }
            // Check blacklist
            TokenBlacklistService tokenBlacklistService = getBean(TokenBlacklistService.class, request);
            if (tokenBlacklistService != null && tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Rejected request because token is blacklisted");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                return;
            }
            String username = authUtil.getUsernameFromToken(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user == null) {
                    log.warn("User not found for token subject: {}", username);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found for token subject");
                    return;
                }
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                        = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private <T> T getBean(Class<T> beanClass, jakarta.servlet.http.HttpServletRequest request) {
        try {
            return org.springframework.web.context.support.WebApplicationContextUtils
                    .getRequiredWebApplicationContext(request.getServletContext())
                    .getBean(beanClass);
        } catch (Exception e) {
            return null;
        }
    }
}