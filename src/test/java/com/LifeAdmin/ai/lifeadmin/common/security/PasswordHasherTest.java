package com.LifeAdmin.ai.lifeadmin.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PasswordHasher}: BCrypt hash/verify round-trip without
 * storing the plaintext password.
 *
 * <p>Validates: Requirements 3.4
 */
class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashThenMatchesReturnsTrueForCorrectPassword() {
        String raw = "S3cure-P@ssw0rd";
        String stored = hasher.hash(raw);

        assertThat(hasher.matches(raw, stored)).isTrue();
    }

    @Test
    void matchesReturnsFalseForWrongPassword() {
        String stored = hasher.hash("S3cure-P@ssw0rd");

        assertThat(hasher.matches("wrong-password", stored)).isFalse();
    }

    @Test
    void hashDoesNotEqualRawPasswordSoPlaintextIsNeverStored() {
        String raw = "S3cure-P@ssw0rd";

        String stored = hasher.hash(raw);

        assertThat(stored).isNotEqualTo(raw);
        assertThat(stored).doesNotContain(raw);
    }

    @Test
    void matchesReturnsFalseForBlankInputs() {
        String stored = hasher.hash("S3cure-P@ssw0rd");

        assertThat(hasher.matches("", stored)).isFalse();
        assertThat(hasher.matches("S3cure-P@ssw0rd", "")).isFalse();
    }
}
