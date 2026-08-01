package com.LifeAdmin.ai.lifeadmin.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LifeAdmin.ai.lifeadmin.auth.domain.RefreshToken;

/**
 * Persistence port for {@link RefreshToken} entities.
 *
 * <p>Presented refresh tokens are matched against stored hashes via
 * {@link #findByTokenHash(String)}; the raw token value is never stored or queried
 * (Req 3.4).
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token by the hash of its raw value (Req 3.1, 3.4).
     *
     * @param tokenHash the stored hash of the presented refresh token
     * @return the matching refresh token, if present
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
