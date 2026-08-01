package com.LifeAdmin.ai.lifeadmin.common.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code lifeadmin.security.*} configuration used by the token
 * infrastructure (Req 2.1, 32.3).
 *
 * <ul>
 *   <li>{@code lifeadmin.security.jwt-secret} — HMAC secret sourced from the
 *       {@code JWT_SECRET} environment variable.</li>
 *   <li>{@code lifeadmin.security.access-token-ttl} — Access_Token lifetime
 *       (e.g. {@code 15m}).</li>
 *   <li>{@code lifeadmin.security.refresh-token-ttl} — Refresh_Token lifetime
 *       (e.g. {@code 30d}); consumed by the auth module.</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "lifeadmin.security")
public class SecurityProperties {

    /** HMAC signing secret for Access_Tokens (from JWT_SECRET). */
    private String jwtSecret = "";

    /** Access_Token time-to-live. Defaults to 15 minutes. */
    private Duration accessTokenTtl = Duration.ofMinutes(15);

    /** Refresh_Token time-to-live. Defaults to 30 days. */
    private Duration refreshTokenTtl = Duration.ofDays(30);

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }
}
