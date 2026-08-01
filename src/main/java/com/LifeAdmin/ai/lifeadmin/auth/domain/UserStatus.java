package com.LifeAdmin.ai.lifeadmin.auth.domain;

/**
 * Lifecycle status of a {@link User} account.
 *
 * <p>Stored as a string in the {@code users.status} column
 * (see {@code @Enumerated(EnumType.STRING)}). New users are created
 * {@link #ACTIVE} (Req 1.1); authentication is only permitted for ACTIVE users,
 * while {@link #LOCKED} and {@link #DISABLED} accounts are rejected with
 * {@code ACCOUNT_NOT_ACTIVE} (Req 2.4).
 */
public enum UserStatus {
    ACTIVE,
    LOCKED,
    DISABLED
}
