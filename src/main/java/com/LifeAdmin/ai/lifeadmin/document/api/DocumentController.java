package com.LifeAdmin.ai.lifeadmin.document.api;


import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.document.application.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * DocumentController: manages document operations.
 * Requirement 7-10, 35
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService, CurrentUserProvider currentUserProvider) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            DocumentResponse response = documentService.upload(file);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> list(Pageable pageable) {
        Page<DocumentResponse> documents = documentService.list(pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> get(@PathVariable String documentId) {
        DocumentResponse document = documentService.get(documentId);
        return ResponseEntity.ok(document);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable String documentId) {
        documentService.delete(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/reprocess")
    public ResponseEntity<Void> reprocess(@PathVariable String documentId) {
        documentService.reprocess(documentId);
        return ResponseEntity.accepted().build();
    }
}
