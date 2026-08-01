package com.LifeAdmin.ai.lifeadmin.obligation.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Obligation: a responsibility extracted from a document or created by user.
 * Requirement 15, 17, 18
 *
 * Dedup identity: (document_id, type, normalized_due_date, normalized_reference)
 * Database unique constraint enforces this composite key.
 */
@Entity
@Table(name = "obligations", indexes = {
    @Index(name = "idx_obligations_user_id", columnList = "user_id"),
    @Index(name = "idx_obligations_document_id", columnList = "document_id")
}, uniqueConstraints = {
    @UniqueConstraint(
        name = "uq_obligations_dedup_identity",
        columnNames = {"document_id", "type", "normalized_due_date", "normalized_reference"}
    )
})
public class Obligation extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "document_id")
    private UUID documentId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "type", nullable = false)
    private String type;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "confidence", nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ObligationStatus status;

    @NotNull
    @Column(name = "requires_confirmation", nullable = false)
    private Boolean requiresConfirmation = false;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Dedup identity columns (Requirement 17)
    @Column(name = "normalized_due_date")
    private LocalDate normalizedDueDate;

    @Column(name = "normalized_reference")
    private String normalizedReference;

    // Constructors
    public Obligation() {}

    public Obligation(UUID userId, UUID documentId, String type, String title,
                     Priority priority, Double confidence, SourceType sourceType) {
        this.userId = userId;
        this.documentId = documentId;
        this.type = type;
        this.title = title;
        this.priority = priority;
        this.confidence = confidence;
        this.sourceType = sourceType;
        this.status = ObligationStatus.DETECTED;
        this.requiresConfirmation = false;
    }

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ObligationStatus getStatus() {
        return status;
    }

    public void setStatus(ObligationStatus status) {
        this.status = status;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(Instant dismissedAt) {
        this.dismissedAt = dismissedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDate getNormalizedDueDate() {
        return normalizedDueDate;
    }

    public void setNormalizedDueDate(LocalDate normalizedDueDate) {
        this.normalizedDueDate = normalizedDueDate;
    }

    public String getNormalizedReference() {
        return normalizedReference;
    }

    public void setNormalizedReference(String normalizedReference) {
        this.normalizedReference = normalizedReference;
    }
}
