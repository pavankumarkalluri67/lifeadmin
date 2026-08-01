package com.LifeAdmin.ai.lifeadmin.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and verifies short-lived JWT Access_Tokens (Req 2.1).
 *
 * <p>Each token embeds the user's {@link UUID} as the JWT subject and carries
 * an expiry derived from {@code lifeadmin.security.access-token-ttl}. The token
 * is signed with HMAC-SHA256 using a 256-bit key derived from the configured
 * secret. Verification exposes the {@code userId} and expiry and reports
 * expired/invalid tokens via {@link InvalidTokenException}.
 */
@Service
public class TokenService {

    private final SecretKey signingKey;
    private final java.time.Duration accessTokenTtl;

    public TokenService(SecurityProperties properties) {
        this.signingKey = deriveKey(properties.getJwtSecret());
        this.accessTokenTtl = properties.getAccessTokenTtl();
    }

    /**
     * Issues a signed Access_Token for the given user, valid for the configured
     * access-token TTL.
     *
     * @param userId the authenticated user's identifier (becomes the subject)
     * @return the compact, signed JWT string
     */
    public String issueAccessToken(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenTtl);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies and parses an Access_Token, returning the subject user id and
     * expiry.
     *
     * @param token the compact JWT string
     * @return the verified user id and expiry
     * @throws InvalidTokenException if the token is blank, malformed, has an
     *                               invalid signature, is expired, or carries a
     *                               non-UUID subject
     */
    public VerifiedToken verify(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Access token is missing");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = parseSubject(claims.getSubject());
            Instant expiresAt = claims.getExpiration() != null
                    ? claims.getExpiration().toInstant()
                    : null;
            return new VerifiedToken(userId, expiresAt);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Access token is expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Access token is invalid", e);
        }
    }

    private static UUID parseSubject(String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new InvalidTokenException("Access token has no subject");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Access token subject is not a valid user id", e);
        }
    }

    /**
     * Derives a stable 256-bit HMAC key from the configured secret. Hashing the
     * secret with SHA-256 guarantees a key of the length HS256 requires,
     * regardless of the raw secret's length.
     */
    private static SecretKey deriveKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT secret is not configured (set JWT_SECRET / lifeadmin.security.jwt-secret)");
        }
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
