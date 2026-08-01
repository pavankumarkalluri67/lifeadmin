package com.LifeAdmin.ai.lifeadmin.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response body returned for every failed request (Req 29.1).
 *
 * <p>Contains {@code timestamp}, {@code status}, {@code code}, {@code message},
 * {@code path}, and {@code traceId}. For Bean Validation failures the
 * {@code errors} array carries one {@link FieldError} per invalid field
 * (Req 29.2). The {@code errors} array is omitted from serialization when empty.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        List<FieldError> errors) {

    /** A single field-level validation error entry (Req 29.2). */
    public record FieldError(String field, String message) {
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link ApiError}. */
    public static final class Builder {
        private Instant timestamp = Instant.now();
        private int status;
        private String code;
        private String message;
        private String path;
        private String traceId;
        private List<FieldError> errors = List.of();

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder errors(List<FieldError> errors) {
            this.errors = errors == null ? List.of() : errors;
            return this;
        }

        public ApiError build() {
            return new ApiError(timestamp, status, code, message, path, traceId, errors);
        }
    }
}
