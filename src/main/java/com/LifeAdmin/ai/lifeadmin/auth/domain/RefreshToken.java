package com.LifeAdmin.ai.lifeadmin.auth.domain;

import java.time.Instant;

import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A long-lived refresh token issued to a {@link User}.
 *
 * <p>Maps to the {@code refresh_tokens} table defined in
 * {@code V1__initial_schema.sql}. Inherits {@code id}, {@code version},
 * {@code created_at}, and {@code updated_at} from {@link BaseEntity}.
 *
 * <p>The raw refresh token value is never persisted; only its hash is stored in
 * {@link #tokenHash} (Req 2.5, 3.4). Tokens are revoked (not deleted) on rotation
 * and logout by setting {@link #revoked} to {@code true} and recording
 * {@link #revokedAt} (Req 3.2, 4.1).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshToken() {
        // Required by JPA.
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
