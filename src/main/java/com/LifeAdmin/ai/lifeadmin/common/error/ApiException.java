package com.LifeAdmin.ai.lifeadmin.common.error;

import org.springframework.http.HttpStatus;

/**
 * Base type for domain exceptions that carry a standardized {@link ErrorCode}.
 *
 * <p>Throwing an {@code ApiException} (or a subclass) lets the
 * {@link GlobalExceptionHandler} produce a consistent {@link ApiError} response
 * without per-controller handling (Req 29.3). Modules may throw this directly
 * with any {@link ErrorCode}, or use one of the shared subclasses.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public HttpStatus status() {
        return errorCode.defaultStatus();
    }
}
