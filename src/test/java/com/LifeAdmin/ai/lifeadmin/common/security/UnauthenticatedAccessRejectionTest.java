package com.LifeAdmin.ai.lifeadmin.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LifeAdmin.ai.lifeadmin.common.config.SecurityConfig;
import com.LifeAdmin.ai.lifeadmin.common.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for authentication enforcement on protected endpoints
 * (Requirement 6.1).
 *
 * <p>Uses a {@code @WebMvcTest} slice so only the web + security layer boots —
 * no JPA, DataSource, or Flyway auto-configuration is loaded, which means the
 * test runs without a live PostgreSQL. The real {@link SecurityConfig} filter
 * chain, {@link JwtAuthenticationFilter}, {@link TokenService},
 * {@link RestAuthenticationEntryPoint}, and {@link SecurityProperties} are wired
 * in exactly as they run in production, and a minimal test-only controller
 * exposes a protected path ({@code /api/v1/ping}).
 *
 * <p>Asserts that a request without a valid Access_Token is rejected with a
 * 401 response whose standardized error body carries code
 * {@code UNAUTHENTICATED}.
 */
@WebMvcTest(controllers = UnauthenticatedAccessRejectionTest.PingController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        TokenService.class,
        UnauthenticatedAccessRejectionTest.TestBeans.class
})
@EnableConfigurationProperties(SecurityProperties.class)
class UnauthenticatedAccessRejectionTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Supplies the Jackson {@link ObjectMapper} that
     * {@link RestAuthenticationEntryPoint} depends on to serialize the error
     * body. The {@code @WebMvcTest} slice does not expose it as a bean by
     * default, so it is provided here explicitly.
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class TestBeans {

        @Bean
        ObjectMapper objectMapper() {
            // findAndRegisterModules() picks up the JSR-310 module so the
            // ApiError.timestamp (java.time.Instant) serializes correctly,
            // mirroring the Boot-configured mapper used in production.
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @Test
    void rejectsProtectedEndpointWhenNoTokenIsPresent() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.name()))
                .andExpect(jsonPath("$.path").value("/api/v1/ping"));
    }

    @Test
    void rejectsProtectedEndpointWhenBearerTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/ping")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.name()));
    }

    /**
     * Minimal protected controller used only by this test to expose a path that
     * is not on the public allow-list, so it exercises the authenticated-only
     * branch of the security filter chain.
     */
    @RestController
    static class PingController {

        @GetMapping("/api/v1/ping")
        String ping() {
            return "pong";
        }
    }
}
