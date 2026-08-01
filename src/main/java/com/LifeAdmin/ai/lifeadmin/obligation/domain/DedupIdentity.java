package com.LifeAdmin.ai.lifeadmin.obligation.domain;

import java.time.LocalDate;

/**
 * DedupIdentity: composite key for obligation deduplication.
 * Requirement 17
 *
 * Components:
 * 1. documentId
 * 2. type
 * 3. normalizedDueDate (truncated to calendar day)
 * 4. normalizedReference (trimmed, lowercased)
 */
public class DedupIdentity {

    private final String documentId;
    private final String type;
    private final LocalDate normalizedDueDate;
    private final String normalizedReference;

    public DedupIdentity(String documentId, String type, LocalDate normalizedDueDate, String normalizedReference) {
        this.documentId = documentId;
        this.type = type;
        this.normalizedDueDate = normalizedDueDate;
        this.normalizedReference = normalizedReference;
    }

    public static LocalDate normalizeDate(LocalDate date) {
        // Already a calendar date (no time component)
        return date;
    }

    public static String normalizeReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return "";
        }
        return reference.trim().toLowerCase();
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getType() {
        return type;
    }

    public LocalDate getNormalizedDueDate() {
        return normalizedDueDate;
    }

    public String getNormalizedReference() {
        return normalizedReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DedupIdentity that = (DedupIdentity) o;
        return documentId.equals(that.documentId) &&
               type.equals(that.type) &&
               java.util.Objects.equals(normalizedDueDate, that.normalizedDueDate) &&
               java.util.Objects.equals(normalizedReference, that.normalizedReference);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(documentId, type, normalizedDueDate, normalizedReference);
    }
}
