package com.LifeAdmin.ai.lifeadmin.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TokenService}: issue/verify round-trip, rejection of
 * tampered tokens, and expiry handling. Uses a plain in-code
 * {@link SecurityProperties} instance (no Spring context, no DB).
 *
 * <p>Validates: Requirements 2.1
 */
class TokenServiceTest {

    private static SecurityProperties properties(String secret, Duration accessTtl) {
        SecurityProperties props = new SecurityProperties();
        props.setJwtSecret(secret);
        props.setAccessTokenTtl(accessTtl);
        return props;
    }

    private static TokenService serviceWithTtl(Duration accessTtl) {
        return new TokenService(properties("test-secret-for-token-service-unit-tests", accessTtl));
    }

    @Test
    void issueThenVerifyRoundTripReturnsSameUserId() {
        TokenService service = serviceWithTtl(Duration.ofMinutes(15));
        UUID userId = UUID.randomUUID();

        String token = service.issueAccessToken(userId);
        VerifiedToken verified = service.verify(token);

        assertThat(verified.userId()).isEqualTo(userId);
        assertThat(verified.expiresAt()).isNotNull();
    }

    @Test
    void verifyRejectsGarbageToken() {
        TokenService service = serviceWithTtl(Duration.ofMinutes(15));

        assertThatThrownBy(() -> service.verify("not-a-real-jwt"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyRejectsTamperedToken() {
        TokenService service = serviceWithTtl(Duration.ofMinutes(15));
        String token = service.issueAccessToken(UUID.randomUUID());

        // Flip the final character to corrupt the signature.
        char last = token.charAt(token.length() - 1);
        char replacement = last == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 1) + replacement;

        assertThatThrownBy(() -> service.verify(tampered))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyRejectsBlankToken() {
        TokenService service = serviceWithTtl(Duration.ofMinutes(15));

        assertThatThrownBy(() -> service.verify("   "))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyRejectsTokenSignedWithDifferentSecret() {
        TokenService issuer = new TokenService(properties("secret-one", Duration.ofMinutes(15)));
        TokenService otherVerifier = new TokenService(properties("secret-two", Duration.ofMinutes(15)));
        String token = issuer.issueAccessToken(UUID.randomUUID());

        assertThatThrownBy(() -> otherVerifier.verify(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyTreatsExpiredTokenAsInvalid() {
        // A negative TTL issues a token whose expiry is already in the past,
        // so verification must reject it as expired deterministically.
        TokenService service = serviceWithTtl(Duration.ofSeconds(-60));
        String token = service.issueAccessToken(UUID.randomUUID());

        assertThatThrownBy(() -> service.verify(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void issueRejectsNullUserId() {
        TokenService service = serviceWithTtl(Duration.ofMinutes(15));

        assertThatThrownBy(() -> service.issueAccessToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
