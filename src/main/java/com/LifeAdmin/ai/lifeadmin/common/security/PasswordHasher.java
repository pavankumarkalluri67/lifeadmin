package com.LifeAdmin.ai.lifeadmin.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Hashes and verifies user passwords using BCrypt so that only the hash is
 * persisted and the plaintext password is never stored or returned (Req 1.2).
 */
@Component
public class PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Produces a BCrypt hash of the raw password.
     *
     * @param rawPassword the plaintext password
     * @return the BCrypt-encoded hash
     */
    public String hash(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * Verifies a raw password against a previously encoded BCrypt hash.
     *
     * @param rawPassword    the plaintext password to check
     * @param encodedPassword the stored BCrypt hash
     * @return {@code true} if the password matches the hash
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}
