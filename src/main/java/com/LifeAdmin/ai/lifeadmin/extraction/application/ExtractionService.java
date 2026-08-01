package com.LifeAdmin.ai.lifeadmin.extraction.application;



import com.LifeAdmin.ai.lifeadmin.ai.domain.DocumentAnalysisResult;
import com.LifeAdmin.ai.lifeadmin.document.domain.Document;
import com.LifeAdmin.ai.lifeadmin.document.domain.DocumentContents;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentContentsRepository;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentRepository;
import com.LifeAdmin.ai.lifeadmin.extraction.domain.ExtractedEntity;
import com.LifeAdmin.ai.lifeadmin.extraction.domain.TextExtractor;
import com.LifeAdmin.ai.lifeadmin.extraction.repository.ExtractedEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/**
 * Extraction_Service: text extraction and entity persistence.
 * Requirement 12, 14
 */
@Service
public class ExtractionService {

    private final DocumentContentsRepository documentContentsRepository;
    private final ExtractedEntityRepository extractedEntityRepository;
    private final DocumentRepository documentRepository;

    private static final Set<String> ALLOWED_ENTITY_TYPES = Set.of(
        "DATE", "AMOUNT", "PERSON", "ORGANIZATION", "LOCATION", "DURATION",
        "PERCENTAGE", "REFERENCE_NUMBER", "EMAIL", "PHONE", "URL"
    );

    public ExtractionService(DocumentContentsRepository documentContentsRepository,
                            ExtractedEntityRepository extractedEntityRepository,
                            DocumentRepository documentRepository) {
        this.documentContentsRepository = documentContentsRepository;
        this.extractedEntityRepository = extractedEntityRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void extract(Document document, byte[] fileBytes) throws IOException {
        String extractionMethod;
        String rawText;

        try {
            if ("application/pdf".equals(document.getContentType())) {
                rawText = TextExtractor.extractFromPdf(fileBytes);
                extractionMethod = "PDFBOX";
            } else if ("text/plain".equals(document.getContentType())) {
                rawText = TextExtractor.extractFromPlainText(fileBytes);
                extractionMethod = "PLAIN_TEXT";
            } else {
                throw new IllegalArgumentException("Unsupported content type for extraction");
            }
        } catch (Exception e) {
            throw new IOException("Text extraction failed", e);
        }

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("EXTRACTION_EMPTY");
        }

        String normalizedText = TextExtractor.normalizeText(rawText);

        // Persist document contents
        DocumentContents contents = new DocumentContents(
            document.getId(),
            rawText,
            normalizedText,
            normalizedText.length(),
            extractionMethod
        );

        documentContentsRepository.save(contents);
    }

    @Transactional
    public void persistEntities(java.util.UUID documentId, DocumentAnalysisResult result) {
        // Delete prior entities (for reprocess)
        extractedEntityRepository.deleteByDocumentId(documentId);

        if (result.entities() == null || result.entities().isEmpty()) {
            return;
        }

        for (DocumentAnalysisResult.EntityCandidate entityCandidate : result.entities()) {
            // Validate entity type
            if (!ALLOWED_ENTITY_TYPES.contains(entityCandidate.entityType())) {
                // Skip unknown entity type
                continue;
            }

            ExtractedEntity entity = new ExtractedEntity(
                documentId,
                entityCandidate.entityType(),
                entityCandidate.entityValue(),
                entityCandidate.confidence(),
                "AI"  // extraction_method
            );

            entity.setNormalizedValue(entityCandidate.normalizedValue());
            extractedEntityRepository.save(entity);
        }
    }

    @Transactional(readOnly = true)
    public String retrieveText(java.util.UUID documentId) {
        return documentContentsRepository.findByDocumentId(documentId)
            .map(DocumentContents::getNormalizedText)
            .orElse("");
    }
}
