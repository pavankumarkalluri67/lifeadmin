package com.LifeAdmin.ai.lifeadmin.processing.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

/**
 * ProcessingJob: database-backed job record for asynchronous document processing.
 * Enables recovery on restart and retry logic with exponential backoff.
 * Requirement 11, 16
 */
@Entity
@Table(name = "processing_jobs", indexes = {
    @Index(name = "idx_processing_jobs_document_id", columnList = "document_id")
})
public class ProcessingJob extends BaseEntity {

    @NotNull
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessingJobStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    private ProcessingStage currentStage;

    @NotNull
    @Min(0)
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @NotNull
    @Min(1)
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Size(max = 64)
    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Constructors
    public ProcessingJob() {}

    public ProcessingJob(UUID documentId) {
        this.documentId = documentId;
        this.status = ProcessingJobStatus.PENDING;
        this.currentStage = ProcessingStage.QUEUED;
        this.attemptCount = 0;
        this.maxAttempts = 3;
    }

    // Getters and setters
    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public ProcessingJobStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingJobStatus status) {
        this.status = status;
    }

    public ProcessingStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(ProcessingStage currentStage) {
        this.currentStage = currentStage;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
