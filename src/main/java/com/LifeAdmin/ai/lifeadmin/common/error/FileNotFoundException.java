package com.LifeAdmin.ai.lifeadmin.common.error;

/**
 * Thrown when a stored file backing a document cannot be located by the
 * storage adapter. Maps to 404 {@link ErrorCode#FILE_NOT_FOUND} (Req 35.3).
 */
public class FileNotFoundException extends ApiException {

    public FileNotFoundException(String message) {
        super(ErrorCode.FILE_NOT_FOUND, message);
    }
}
