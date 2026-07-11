package com.example.WordGame.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
// import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.example.WordGame.modules.auth.service.JwtAuthFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
@EnableWebMvc
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) throws Exception {

        httpSecurity
                .csrf(csrfConfig -> csrfConfig.disable())
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll()  // Login, Register
                        .requestMatchers("/api/v1/login").permitAll()
                        // allow public reads of genres and legal documents
                        .requestMatchers(HttpMethod.GET, "/api/genres/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/legal-documents").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/legal-documents/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/legal-documents").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/legal-documents/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/legal-documents/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/legal-documents/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/legal-documents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me/consents").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/{userId}/consents").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/words/**").permitAll()
                        // Only admins can create admin-scoped resources
                        .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/genres").hasRole("ADMIN")
                        // Editors (and admins) can manage editor-scoped resources
                        .requestMatchers(HttpMethod.POST, "/api/editor/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/editor/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()


                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/public/**").permitAll()

                        
                        // Allow OAuth2 authorization endpoints (used by Spring Security)
                        .requestMatchers("/oauth2/authorization/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // Only configure oauth2Login if a ClientRegistrationRepository is present (i.e., oauth2 client is configured)
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            httpSecurity.oauth2Login(oAuth2 -> oAuth2.failureHandler((request, response, exception) -> {
                log.error("OAuth2 error: {}", exception.getMessage());
            }));
        }

                // For API endpoints, don't redirect to OAuth login page — return 401 instead
                httpSecurity.exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                ));

        return httpSecurity.build();
    }
}