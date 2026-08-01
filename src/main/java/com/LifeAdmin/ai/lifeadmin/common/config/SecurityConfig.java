package com.LifeAdmin.ai.lifeadmin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.LifeAdmin.ai.lifeadmin.common.security.JwtAuthenticationFilter;
import com.LifeAdmin.ai.lifeadmin.common.security.RestAuthenticationEntryPoint;

/**
 * Spring Security configuration for the stateless JWT-secured API (Req 6.1,
 * 24.4, 26.2).
 *
 * <ul>
 *   <li>Sessions are stateless — identity comes only from the Access_Token.</li>
 *   <li>CSRF is disabled: there are no cookies/sessions to protect and the API
 *       is token-authenticated.</li>
 *   <li>Auth endpoints, the actuator health probe, and the OpenAPI/Swagger
 *       surface are public; everything else requires authentication.</li>
 *   <li>The {@link JwtAuthenticationFilter} runs before the username/password
 *       filter to populate the security context from the Bearer token.</li>
 *   <li>Unauthenticated access to protected endpoints is rejected with a
 *       standardized 401 {@code UNAUTHENTICATED} body by the
 *       {@link RestAuthenticationEntryPoint}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/actuator/health",
            "/actuator/health/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling ->
                        handling.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
