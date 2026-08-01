package com.LifeAdmin.ai.lifeadmin.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TokenHasher}: refresh-token hash comparison without
 * storing the raw value.
 *
 * <p>Validates: Requirements 3.4
 */
class TokenHasherTest {

    private final TokenHasher hasher = new TokenHasher();

    @Test
    void matchesReturnsTrueForCorrectRawToken() {
        String raw = "refresh-token-abc-123";
        String stored = hasher.hash(raw);

        assertThat(hasher.matches(raw, stored)).isTrue();
    }

    @Test
    void matchesReturnsFalseForWrongRawToken() {
        String stored = hasher.hash("refresh-token-abc-123");

        assertThat(hasher.matches("refresh-token-xyz-999", stored)).isFalse();
    }

    @Test
    void hashDoesNotEqualRawTokenSoRawIsNeverStored() {
        String raw = "refresh-token-abc-123";

        String stored = hasher.hash(raw);

        assertThat(stored).isNotEqualTo(raw);
        assertThat(stored).doesNotContain(raw);
    }

    @Test
    void hashIsDeterministicForSameInput() {
        String raw = "refresh-token-abc-123";

        assertThat(hasher.hash(raw)).isEqualTo(hasher.hash(raw));
    }

    @Test
    void matchesReturnsFalseForBlankInputs() {
        String stored = hasher.hash("refresh-token-abc-123");

        assertThat(hasher.matches("", stored)).isFalse();
        assertThat(hasher.matches("refresh-token-abc-123", "")).isFalse();
    }
}
