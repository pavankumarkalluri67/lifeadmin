package com.LifeAdmin.ai.lifeadmin.document.application;


import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.document.api.DocumentResponse;
import com.LifeAdmin.ai.lifeadmin.document.domain.Document;
import com.LifeAdmin.ai.lifeadmin.document.domain.UploadValidator;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentRepository;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.repository.ProcessingJobRepository;
import com.LifeAdmin.ai.lifeadmin.storage.domain.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Document_Service: upload, list, retrieve, download, delete, reprocess.
 * Requirement 7, 8, 9, 10, 35
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final StorageService storageService;
    private final CurrentUserProvider currentUserProvider;

    public DocumentService(DocumentRepository documentRepository,
                          ProcessingJobRepository processingJobRepository,
                          StorageService storageService,
                          CurrentUserProvider currentUserProvider) {
        this.documentRepository = documentRepository;
        this.processingJobRepository = processingJobRepository;
        this.storageService = storageService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public DocumentResponse upload(MultipartFile file) throws IOException {
        UUID userId = currentUserProvider.getUserId();
        
        byte[] fileBytes = file.getBytes();
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // Validate upload
        try {
            UploadValidator.validate(originalFileName, contentType, fileBytes, null);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // Compute SHA-256 checksum
        String checksumSha256 = computeSha256(fileBytes);

        // Check for per-user duplicate
        if (documentRepository.findByChecksumSha256AndUserId(checksumSha256, userId).isPresent()) {
            throw new IllegalArgumentException("Document already uploaded by this user");
        }

        // Store file
        String storageKey = storageService.store(fileBytes, originalFileName);

        // Create Document record
        String sanitizedFileName = UploadValidator.sanitizeFileName(originalFileName);
        String storedFileName = UUID.randomUUID() + "_" + sanitizedFileName;

        Document document = new Document(
            userId,
            sanitizedFileName,
            storedFileName,
            storageKey,
            contentType,
            getFileExtension(originalFileName),
            (long) fileBytes.length,
            checksumSha256
        );

        documentRepository.save(document);

        // Create Processing Job
        ProcessingJob job = new ProcessingJob(document.getId());
        processingJobRepository.save(job);

        return mapToResponse(document);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> list(Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        return documentRepository.findByUserId(userId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(String documentId) {
        UUID userId = currentUserProvider.getUserId();
        UUID id = UUID.fromString(documentId);
        Document document = documentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return mapToResponse(document);
    }

    @Transactional
    public void delete(String documentId) {
        UUID userId = currentUserProvider.getUserId();
        UUID id = UUID.fromString(documentId);
        Document document = documentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Delete stored file
        storageService.delete(document.getStorageKey());

        // Delete document and associated processing job
        processingJobRepository.findByDocumentId(id).ifPresent(processingJobRepository::delete);

        documentRepository.delete(document);
    }

    @Transactional
    public void reprocess(String documentId) {
        UUID userId = currentUserProvider.getUserId();
        UUID id = UUID.fromString(documentId);
        Document document = documentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Create or reset processing job
        ProcessingJob job = processingJobRepository.findByDocumentId(id)
            .orElse(new ProcessingJob(id));

        job.setAttemptCount(0);
        processingJobRepository.save(job);
    }

    private String computeSha256(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
            .id(document.getId().toString())
            .originalFileName(document.getOriginalFileName())
            .storedFileName(document.getStoredFileName())
            .contentType(document.getContentType())
            .fileSizeBytes(document.getFileSizeBytes())
            .checksumSha256(document.getChecksumSha256())
            .documentType(document.getDocumentType())
            .processingStatus(document.getProcessingStatus().toString())
            .processingErrorCode(document.getProcessingErrorCode())
            .uploadedAt(document.getUploadedAt())
            .build();
    }
}
