package com.LifeAdmin.ai.lifeadmin.ai.domain;

import jakarta.validation.ConstraintViolation;

import java.util.Collections;
import java.util.Set;

/**
 * Raised when a {@link DocumentAnalysisResult} fails Java validation before
 * persistence (Req 13.2, 13.3).
 *
 * <p>This is an internal domain signal, not an API-facing error: the
 * Processing_Service treats an invalid result as a failed analysis and applies
 * the retry policy defined in Requirement 16 (Req 13.3). The offending
 * constraint violations are retained for auditing and diagnostics.
 */
public class InvalidAnalysisResultException extends RuntimeException {

    private final transient Set<ConstraintViolation<DocumentAnalysisResult>> violations;

    public InvalidAnalysisResultException(Set<ConstraintViolation<DocumentAnalysisResult>> violations) {
        super("DocumentAnalysisResult failed validation: " + summarize(violations));
        this.violations = violations == null ? Set.of() : Set.copyOf(violations);
    }

    /**
     * @return the unmodifiable set of constraint violations that caused the failure
     */
    public Set<ConstraintViolation<DocumentAnalysisResult>> getViolations() {
        return Collections.unmodifiableSet(violations);
    }

    private static String summarize(Set<ConstraintViolation<DocumentAnalysisResult>> violations) {
        if (violations == null || violations.isEmpty()) {
            return "no details";
        }
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<DocumentAnalysisResult> v : violations) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(v.getPropertyPath()).append(' ').append(v.getMessage());
        }
        return sb.toString();
    }
}
