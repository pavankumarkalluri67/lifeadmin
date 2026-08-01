package com.LifeAdmin.ai.lifeadmin.ai.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Structured AI output for document analysis (Req 13.1).
 *
 * <p>This is a <strong>transient</strong> DTO produced by the LLM. It carries no
 * JPA/persistence semantics and MUST pass Java Bean Validation plus domain rules
 * before any persistence occurs (Req 13.2, 13.3). Validation is performed by
 * {@link DocumentAnalysisValidator}.
 *
 * <p>Confidence values are decimals in the inclusive range {@code [0, 1]}
 * (see the {@code Confidence} definition in the requirements glossary).
 *
 * @param documentType the classified document type (never blank)
 * @param confidence   overall classification confidence in {@code [0, 1]}
 * @param entities      structured entity candidates extracted from the text
 * @param obligations   obligation candidates detected in the text
 */
public record DocumentAnalysisResult(

        @NotBlank
        String documentType,

        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "1.0")
        Double confidence,

        @NotNull
        @Valid
        List<EntityCandidate> entities,

        @NotNull
        @Valid
        List<ObligationCandidate> obligations
) {


    /**
     * A structured value proposed by the AI (for example an amount or a date).
     * Validated before being persisted as an {@code ExtractedEntity} (Req 14).
     *
     * @param entityType      the entity type (validated against the allowed set later, Req 14.3)
     * @param entityValue     the raw value as found in the text
     * @param normalizedValue an optional normalized form (may be {@code null})
     * @param confidence      extraction confidence in {@code [0, 1]}
     */
    public record EntityCandidate(

            @NotBlank
            String entityType,

            @NotBlank
            String entityValue,

            String normalizedValue,

            @NotNull
            @DecimalMin(value = "0.0")
            @DecimalMax(value = "1.0")
            Double confidence
    ) {
    }

    /**
     * An obligation candidate proposed by the AI. Confidence classification and
     * deduplication are applied downstream by the Obligation_Service (Req 15, 17).
     *
     * @param type       the obligation type (never blank)
     * @param title      a human-readable title (never blank)
     * @param dueDate    an optional due date (maybe {@code null} when unset, Req 15.7)
     * @param priority   an optional priority hint; a default is applied when absent (Req 15.6)
     * @param reference  an optional reference used for deduplication (maybe {@code null})
     * @param confidence detection confidence in {@code [0, 1]}
     */
    public record ObligationCandidate(

            @NotBlank
            String type,

            @NotBlank
            String title,

            LocalDate dueDate,

            String priority,

            String reference,

            @NotNull
            @DecimalMin(value = "0.0")
            @DecimalMax(value = "1.0")
            Double confidence
    ) {
    }
}
