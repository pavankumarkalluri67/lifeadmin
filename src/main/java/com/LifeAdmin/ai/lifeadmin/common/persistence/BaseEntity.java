package com.LifeAdmin.ai.lifeadmin.common.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * Base class for all primary persistent entities.
 *
 * <p>Establishes the shared database conventions for the system:
 * <ul>
 *   <li>UUID primary keys (Req 30.3)</li>
 *   <li>Optimistic locking via a {@code @Version} field (Req 28.1)</li>
 *   <li>{@code created_at} / {@code updated_at} audit timestamps mapped to
 *       {@link Instant} and stored as {@code TIMESTAMPTZ} (Req 30.3, 30.4)</li>
 * </ul>
 *
 * <p>Audit timestamps are populated automatically by Spring Data JPA auditing,
 * which is enabled by {@code JpaAuditingConfig}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Identity is based on the persistent UUID once assigned. Two entities are
     * considered equal only when they are of a compatible type and share a
     * non-null id.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Use the class-level hashCode so instances remain in the same bucket
        // before and after an id is assigned (Hibernate-friendly).
        return getClass().hashCode();
    }
}
