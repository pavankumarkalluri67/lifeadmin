package com.LifeAdmin.ai.lifeadmin.common.error;

import org.springframework.http.HttpStatus;

/**
 * Standardized machine-readable error codes returned to API clients.
 *
 * <p>Each code carries the default HTTP status it maps to so that the
 * {@link GlobalExceptionHandler} and domain exceptions can produce consistent
 * responses (Req 29).
 */
public enum ErrorCode {

    /** Bean Validation failure; accompanied by an {@code errors[]} array (Req 29.2). */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),

    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
    EMPTY_FILE(HttpStatus.BAD_REQUEST),
    CONTENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST),
    DUPLICATE_DOCUMENT(HttpStatus.CONFLICT),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND),

    /** Optimistic locking conflict on stale version update (Req 28.2). */
    STALE_UPDATE(HttpStatus.CONFLICT),

    AI_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus defaultStatus;

    ErrorCode(HttpStatus defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public HttpStatus defaultStatus() {
        return defaultStatus;
    }
}
