package com.LifeAdmin.ai.lifeadmin.ai.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Objects;
import java.util.Set;

/**
 * Pure domain rule that validates a {@link DocumentAnalysisResult} before any
 * persistence occurs (Req 13.2).
 *
 * <p>Validation applies the Jakarta Bean Validation constraints declared on the
 * DTO (including {@code @Valid} nested entity/obligation lists and the
 * {@code [0, 1]} confidence bounds). This class is framework-agnostic: it
 * depends only on the {@code jakarta.validation} API and carries no Spring or
 * provider SDK dependencies, keeping it inside the domain layer.
 *
 * <p>Instances are thread-safe and reusable — a single {@link Validator} may be
 * shared. Prefer injecting a managed {@link Validator} via
 * {@link #DocumentAnalysisValidator(Validator)}; {@link #withDefaultValidator()}
 * is provided for standalone use and tests.
 */
public class DocumentAnalysisValidator {

    private final Validator validator;

    public DocumentAnalysisValidator(Validator validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    /**
     * Build a validator backed by the default Bean Validation provider.
     *
     * @return a ready-to-use {@link DocumentAnalysisValidator}
     */
    public static DocumentAnalysisValidator withDefaultValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return new DocumentAnalysisValidator(factory.getValidator());
    }

    /**
     * Validate a result and return the set of constraint violations.
     *
     * @param result the analysis result to validate (must not be {@code null})
     * @return the constraint violations; empty when the result is valid
     */
    public Set<ConstraintViolation<DocumentAnalysisResult>> validate(DocumentAnalysisResult result) {
        Objects.requireNonNull(result, "result");
        return validator.validate(result);
    }

    /**
     * @param result the analysis result to check (must not be {@code null})
     * @return {@code true} when the result satisfies all validation rules
     */
    public boolean isValid(DocumentAnalysisResult result) {
        return validate(result).isEmpty();
    }

    /**
     * Validate a result, throwing when it is invalid so callers can apply the
     * retry policy (Req 13.3).
     *
     * @param result the analysis result to validate (must not be {@code null})
     * @throws InvalidAnalysisResultException when any validation rule is violated
     */
    public void validateOrThrow(DocumentAnalysisResult result) {
        Set<ConstraintViolation<DocumentAnalysisResult>> violations = validate(result);
        if (!violations.isEmpty()) {
            throw new InvalidAnalysisResultException(violations);
        }
    }
}
