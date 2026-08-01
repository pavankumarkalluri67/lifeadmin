package com.LifeAdmin.ai.lifeadmin.common.error;

/**
 * Thrown when a mutable entity is updated with a stale optimistic-locking
 * version. Maps to 409 {@link ErrorCode#STALE_UPDATE} (Req 28.2).
 */
public class StaleUpdateException extends ApiException {

    public StaleUpdateException(String message) {
        super(ErrorCode.STALE_UPDATE, message);
    }

    public StaleUpdateException(String message, Throwable cause) {
        super(ErrorCode.STALE_UPDATE, message, cause);
    }
}
