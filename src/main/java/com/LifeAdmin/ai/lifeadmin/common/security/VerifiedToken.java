package com.LifeAdmin.ai.lifeadmin.common.security;

import java.time.Instant;
import java.util.UUID;

/**
 * The result of successfully verifying an Access_Token: the authenticated
 * user's UUID (the JWT subject) and the token expiry instant.
 */
public record VerifiedToken(UUID userId, Instant expiresAt) {
}
