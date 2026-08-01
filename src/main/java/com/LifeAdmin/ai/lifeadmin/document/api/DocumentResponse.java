package com.LifeAdmin.ai.lifeadmin.document.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private String id;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSizeBytes;
    private String checksumSha256;
    private String documentType;
    private String processingStatus;
    private String processingErrorCode;
    private Instant uploadedAt;
}
