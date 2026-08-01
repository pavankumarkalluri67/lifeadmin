package com.LifeAdmin.ai.lifeadmin.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Hashes Refresh_Tokens so that only the hash is stored and compared; the raw
 * refresh token value is never persisted (Req 3.4, 2.5).
 *
 * <p>Refresh tokens are high-entropy random values, so a fast SHA-256 hex
 * digest is sufficient (unlike passwords, which require a slow adaptive hash).
 */
@Component
public class TokenHasher {

    /**
     * Computes the SHA-256 hex hash of a raw refresh token.
     *
     * @param rawToken the raw refresh token
     * @return the lowercase hex-encoded SHA-256 hash
     */
    public String hash(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Compares a presented raw refresh token against a stored hash in
     * constant time.
     *
     * @param rawToken   the presented raw refresh token
     * @param storedHash the previously stored hash
     * @return {@code true} if the raw token hashes to the stored hash
     */
    public boolean matches(String rawToken, String storedHash) {
        if (!StringUtils.hasText(rawToken) || !StringUtils.hasText(storedHash)) {
            return false;
        }
        byte[] computed = hash(rawToken).getBytes(StandardCharsets.UTF_8);
        byte[] expected = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(computed, expected);
    }
}
