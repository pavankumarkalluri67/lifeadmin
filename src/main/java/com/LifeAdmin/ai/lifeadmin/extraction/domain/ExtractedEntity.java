package com.LifeAdmin.ai.lifeadmin.extraction.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * ExtractedEntity: a structured value extracted from document text.
 * Requirement 14
 */
@Entity
@Table(name = "extracted_entities", indexes = {
    @Index(name = "idx_extracted_entities_document_id", columnList = "document_id")
})
public class ExtractedEntity extends BaseEntity {

    @NotNull
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @NotBlank
    @Column(name = "entity_value", nullable = false, columnDefinition = "TEXT")
    private String entityValue;

    @Column(name = "normalized_value", columnDefinition = "TEXT")
    private String normalizedValue;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "confidence", nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;

    @NotBlank
    @Column(name = "extraction_method", nullable = false)
    private String extractionMethod;

    @Column(name = "ai_model")
    private String aiModel;

    @Column(name = "prompt_version")
    private String promptVersion;

    // Constructors
    public ExtractedEntity() {}

    public ExtractedEntity(UUID documentId, String entityType, String entityValue,
                          Double confidence, String extractionMethod) {
        this.documentId = documentId;
        this.entityType = entityType;
        this.entityValue = entityValue;
        this.confidence = confidence;
        this.extractionMethod = extractionMethod;
    }

    // Getters and setters
    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(String normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }
}
