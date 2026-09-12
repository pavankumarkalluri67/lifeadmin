package com.LifeAdmin.ai.lifeadmin.processing.application;


import com.LifeAdmin.ai.lifeadmin.ai.domain.AiClient;
import com.LifeAdmin.ai.lifeadmin.ai.domain.DocumentAnalysisResult;
import com.LifeAdmin.ai.lifeadmin.ai.domain.PromptContext;
import com.LifeAdmin.ai.lifeadmin.document.domain.Document;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentRepository;
import com.LifeAdmin.ai.lifeadmin.extraction.application.ExtractionService;
import com.LifeAdmin.ai.lifeadmin.obligation.application.ObligationService;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingStage;
import com.LifeAdmin.ai.lifeadmin.processing.repository.ProcessingJobRepository;
import com.LifeAdmin.ai.lifeadmin.storage.domain.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

/**
 * ProcessingOrchestrator: orchestrates the 6-stage document processing pipeline.
 * Requirement 11, 16
 * Stages:
 * 1. QUEUED - Initial state
 * 2. TEXT_EXTRACTION - Extract text from document
 * 3. TEXT_NORMALIZATION - Normalize extracted text
 * 4. AI_ANALYSIS - Analyze with LLM
 * 5. ENTITY_PERSISTENCE - Save extracted entities
 * 6. OBLIGATION_PERSISTENCE - Save detected obligations
 * 7. COMPLETED - Processing finished
 */
@Service
public class ProcessingOrchestrator {

    private final ProcessingJobRepository processingJobRepository;
    private final DocumentRepository documentRepository;
    private final ExtractionService extractionService;
    private final ProcessingService processingService;
    private final AiClient aiClient;
    private final ObligationService obligationService;
    private final StorageService storageService;

    public ProcessingOrchestrator(ProcessingJobRepository processingJobRepository,
                                 DocumentRepository documentRepository,
                                 ExtractionService extractionService,
                                 ProcessingService processingService,
                                 AiClient aiClient,
                                 ObligationService obligationService,
                                 StorageService storageService) {
        this.processingJobRepository = processingJobRepository;
        this.documentRepository = documentRepository;
        this.extractionService = extractionService;
        this.processingService = processingService;
        this.aiClient = aiClient;
        this.obligationService = obligationService;
        this.storageService = storageService;
    }

    @Transactional
    public void execute(UUID jobId) {
        ProcessingJob job = processingJobRepository.findById(jobId)
            .orElseThrow();

        try {
            processingService.markJobRunning(job);
            
            Document document = documentRepository.findById(job.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

            // Stage 1: TEXT_EXTRACTION
            byte[] fileBytes = getDocumentBytes(document);
            extractionService.extract(document, fileBytes);
            processingService.markStageComplete(job, ProcessingStage.TEXT_NORMALIZATION);

            // Stages 2-3: TEXT_NORMALIZATION (combined with extraction)
            processingService.markStageComplete(job, ProcessingStage.AI_ANALYSIS);

            // Stage 4: AI_ANALYSIS
            String documentText = extractionService.retrieveText(document.getId());
            PromptContext promptContext = new PromptContext("1.0");
            
            DocumentAnalysisResult analysisResult = aiClient.analyze(documentText, promptContext);
            
            // Stages 5-6: ENTITY & OBLIGATION PERSISTENCE
            extractionService.persistEntities(document.getId(), analysisResult);
            obligationService.createFromAnalysis(document.getUserId(), document.getId(), analysisResult);
            
            processingService.markStageComplete(job, ProcessingStage.COMPLETED);
            processingService.markJobCompleted(job);
            
        } catch (IllegalArgumentException e) {
            // Non-retryable errors
            processingService.markJobFailed(job, "EXTRACTION_ERROR", e.getMessage());
        } catch (Exception e) {
            // Retryable errors
            processingService.scheduleRetry(job, "PROCESSING_ERROR", e.getMessage());
        }
    }

    private byte[] getDocumentBytes(Document document) {
        try {
            return storageService.retrieve(document.getStorageKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stored file missing for document " + document.getId()))
                    .getContentAsByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read stored document " + document.getId(), e);
        }
    }
}
