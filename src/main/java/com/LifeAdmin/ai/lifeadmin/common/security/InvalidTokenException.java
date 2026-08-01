package com.LifeAdmin.ai.lifeadmin.common.security;

import com.LifeAdmin.ai.lifeadmin.common.error.ApiException;
import com.LifeAdmin.ai.lifeadmin.common.error.ErrorCode;

/**
 * Raised when an Access_Token is missing, malformed, has an invalid signature,
 * or is expired. Maps to a 401 response with code {@code UNAUTHENTICATED}
 * (Req 6.1). The JWT auth filter (task 3.2) translates this into the API
 * response; the token infrastructure only signals the condition.
 */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(String message) {
        super(ErrorCode.UNAUTHENTICATED, message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHENTICATED, message, cause);
    }
}
