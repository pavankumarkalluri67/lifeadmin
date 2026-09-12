package com.LifeAdmin.ai.lifeadmin.extraction.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

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

    // Getters and setters
    @Setter
    @Getter
    @NotNull
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @NotBlank
    @Size(max = 64)
    @Setter
    @Getter
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @NotBlank
    @Setter
    @Getter
    @Column(name = "entity_value", nullable = false, columnDefinition = "TEXT")
    private String entityValue;

    @Setter
    @Getter
    @Column(name = "normalized_value", columnDefinition = "TEXT")
    private String normalizedValue;

    @Setter
    @Getter
    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "confidence", nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;

    @Setter
    @Getter
    @NotBlank
    @Column(name = "extraction_method", nullable = false)
    private String extractionMethod;

    @Setter
    @Getter
    @Column(name = "ai_model")
    private String aiModel;

    @Getter
    @Setter
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

}
