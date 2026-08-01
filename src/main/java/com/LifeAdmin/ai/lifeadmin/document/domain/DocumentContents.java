package com.LifeAdmin.ai.lifeadmin.document.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

/**
 * DocumentContents: stores extracted and normalized text from a document.
 * One-to-one relationship with Document.
 * Requirement 12
 */
@Entity
@Table(name = "document_contents", uniqueConstraints = {
    @UniqueConstraint(name = "uq_document_contents_document", columnNames = "document_id")
})
public class DocumentContents extends BaseEntity {

    @NotNull
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "normalized_text", columnDefinition = "TEXT")
    private String normalizedText;

    @NotNull
    @Min(0)
    @Column(name = "character_count", nullable = false)
    private Integer characterCount;

    @NotBlank
    @Column(name = "extraction_method", nullable = false)
    private String extractionMethod; // PDFBOX, TIKA, PLAIN_TEXT, OCR, MULTIMODAL_AI

    @NotNull
    @Column(name = "extracted_at", nullable = false)
    private Instant extractedAt;

    // Constructors
    public DocumentContents() {}

    public DocumentContents(UUID documentId, String rawText, String normalizedText,
                           Integer characterCount, String extractionMethod) {
        this.documentId = documentId;
        this.rawText = rawText;
        this.normalizedText = normalizedText;
        this.characterCount = characterCount;
        this.extractionMethod = extractionMethod;
        this.extractedAt = Instant.now();
    }

    // Getters and setters
    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public void setNormalizedText(String normalizedText) {
        this.normalizedText = normalizedText;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(Integer characterCount) {
        this.characterCount = characterCount;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(Instant extractedAt) {
        this.extractedAt = extractedAt;
    }
}
