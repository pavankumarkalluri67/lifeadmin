package com.LifeAdmin.ai.lifeadmin.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LifeAdmin.ai.lifeadmin.auth.domain.User;

/**
 * Persistence port for {@link User} aggregates.
 *
 * <p>Supports email-based lookup for login (Req 2) and uniqueness checks during
 * registration (Req 1.4), in addition to the standard CRUD operations provided by
 * {@link JpaRepository}.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email to look up
     * @return the matching user, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Reports whether a user with the given email already exists (Req 1.4).
     *
     * @param email the email to check
     * @return {@code true} when a user with this email is already registered
     */
    boolean existsByEmail(String email);
}
