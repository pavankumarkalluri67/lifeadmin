package com.LifeAdmin.ai.lifeadmin.document.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Document: represents an uploaded file and tracks its processing status.
 * Requirement 7, 8, 35
 *
 * Unique constraint on (user_id, checksum_sha256) for per-user deduplication.
 * Fields align with database schema: original_file_name, stored_file_name, storage_key,
 * content_type, file_extension, file_size_bytes, checksum_sha256, document_type,
 * processing_status, processing_error_code, uploaded_at.
 */
@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(
        name = "uq_documents_user_checksum",
        columnNames = {"user_id", "checksum_sha256"}
    )
})
public class Document extends BaseEntity {

    @Setter
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @NotBlank
    @Size(max = 512)
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @NotBlank
    @Size(max = 128)
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Size(max = 32)
    @Column(name = "file_extension")
    private String fileExtension;

    @NotNull
    @Min(0)
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @NotBlank
    @Size(max = 64)
    @Column(name = "checksum_sha256", nullable = false)
    private String checksumSha256;

    @NotNull
    @Column(name = "document_type", nullable = false)
    private String documentType; // UNKNOWN until processed

    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @Setter
    @Size(max = 64)
    @Column(name = "processing_error_code")
    private String processingErrorCode;

    @NotNull
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    // Constructors
    public Document() {}

    public Document(UUID userId, String originalFileName, String storedFileName,
                    String storageKey, String contentType, String fileExtension,
                    Long fileSizeBytes, String checksumSha256) {
        this.userId = userId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
        this.fileSizeBytes = fileSizeBytes;
        this.checksumSha256 = checksumSha256;
        this.documentType = "UNKNOWN";
        this.processingStatus = ProcessingStatus.UPLOADED;
        this.uploadedAt = Instant.now();
    }

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public String getDocumentType() {
        return documentType;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public String getProcessingErrorCode() {
        return processingErrorCode;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

}
