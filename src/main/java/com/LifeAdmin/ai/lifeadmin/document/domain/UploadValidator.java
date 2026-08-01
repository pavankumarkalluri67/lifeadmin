package com.LifeAdmin.ai.lifeadmin.document.domain;

import jakarta.validation.constraints.NotBlank;

/**
 * UploadValidator: validation pipeline for document uploads.
 * Requirement 7
 */
public class UploadValidator {

    private static final int MAX_FILE_SIZE = 10485760; // 10 MiB
    private static final String[] ALLOWED_CONTENT_TYPES = {"application/pdf", "text/plain"};

    public static void validate(String originalFileName, String contentType, byte[] fileBytes,
                                String checksumSha256) {
        // 1. Empty file check
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        // 2. Unsupported content type
        if (!isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        // 3. File size check
        if (fileBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of " + MAX_FILE_SIZE + " bytes");
        }

        // 4. Magic byte check (content type mismatch)
        if (!validateMagicBytes(fileBytes, contentType)) {
            throw new IllegalArgumentException("File content does not match declared content type");
        }
    }

    private static boolean isAllowedContentType(String contentType) {
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean validateMagicBytes(byte[] fileBytes, String contentType) {
        if (fileBytes.length < 4) return false;

        byte[] magic = new byte[4];
        System.arraycopy(fileBytes, 0, magic, 0, Math.min(4, fileBytes.length));

        if ("application/pdf".equals(contentType)) {
            // PDF magic bytes: %PDF
            return magic[0] == 0x25 && magic[1] == 0x50 && magic[2] == 0x44 && magic[3] == 0x46;
        }

        if ("text/plain".equals(contentType)) {
            // Plain text - very permissive, any bytes are acceptable
            return true;
        }

        return false;
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        // Remove path separators and control characters
        String sanitized = fileName
            .replaceAll("[/\\\\]", "")  // Remove path separators
            .replaceAll("[\\x00-\\x1F\\x7F]", "");  // Remove control characters

        // Truncate to 255 chars
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        return sanitized.isBlank() ? "document" : sanitized;
    }
}
