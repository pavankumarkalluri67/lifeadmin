package com.LifeAdmin.ai.lifeadmin.common.error;

/**
 * Thrown when a requested resource does not exist or is not owned by the
 * authenticated user. Maps to 404 {@link ErrorCode#RESOURCE_NOT_FOUND}.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resourceType + " not found: " + identifier);
    }
}
