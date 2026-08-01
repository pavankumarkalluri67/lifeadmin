package com.LifeAdmin.ai.lifeadmin.auth.domain;

import java.time.Instant;

import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A registered platform user.
 *
 * <p>Maps to the {@code users} table defined in {@code V1__initial_schema.sql}.
 * Inherits {@code id}, {@code version}, {@code created_at}, and {@code updated_at}
 * from {@link BaseEntity}.
 *
 * <p>The plaintext password is never stored: only the BCrypt hash is persisted in
 * {@link #passwordHash} (Req 1.2). New accounts are created {@link UserStatus#ACTIVE}
 * with {@link #emailVerified} set to {@code false} (Req 1.1, 1.6).
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 320, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected User() {
        // Required by JPA.
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {

    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }
}
