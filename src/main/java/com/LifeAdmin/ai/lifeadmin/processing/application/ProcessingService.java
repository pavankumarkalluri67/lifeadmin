package com.LifeAdmin.ai.lifeadmin.processing.application;



import com.LifeAdmin.ai.lifeadmin.document.domain.Document;
import com.LifeAdmin.ai.lifeadmin.document.domain.ProcessingStatus;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentRepository;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJobStatus;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingStage;
import com.LifeAdmin.ai.lifeadmin.processing.repository.ProcessingJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Processing_Service: orchestrates asynchronous document processing.
 * Requirement 11, 16
 */
@Service
public class ProcessingService {

    private final ProcessingJobRepository processingJobRepository;
    private final DocumentRepository documentRepository;

    public ProcessingService(ProcessingJobRepository processingJobRepository,
                            DocumentRepository documentRepository) {
        this.processingJobRepository = processingJobRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void markJobRunning(ProcessingJob job) {
        job.setStatus(ProcessingJobStatus.RUNNING);
        job.setCurrentStage(ProcessingStage.TEXT_EXTRACTION);
        processingJobRepository.save(job);
    }

    @Transactional
    public void markStageComplete(ProcessingJob job, ProcessingStage nextStage) {
        job.setCurrentStage(nextStage);
        processingJobRepository.save(job);
    }

    @Transactional
    public void markJobCompleted(ProcessingJob job) {
        job.setStatus(ProcessingJobStatus.COMPLETED);
        job.setCurrentStage(ProcessingStage.COMPLETED);
        job.setCompletedAt(Instant.now());
        processingJobRepository.save(job);

        // Update document status
        Document document = documentRepository.findById(job.getDocumentId()).orElse(null);
        if (document != null) {
            document.setProcessingStatus(ProcessingStatus.PROCESSED);
            documentRepository.save(document);
        }
    }

    @Transactional
    public void markJobFailed(ProcessingJob job, String errorCode, String errorMessage) {
        job.setStatus(ProcessingJobStatus.FAILED);
        job.setErrorCode(errorCode);
        job.setErrorMessage(errorMessage);
        processingJobRepository.save(job);

        // Update document status
        Document document = documentRepository.findById(job.getDocumentId()).orElse(null);
        if (document != null) {
            document.setProcessingStatus(ProcessingStatus.FAILED);
            document.setProcessingErrorCode(errorCode);
            documentRepository.save(document);
        }
    }

    @Transactional
    public void scheduleRetry(ProcessingJob job, String errorCode, String errorMessage) {
        job.setAttemptCount(job.getAttemptCount() + 1);
        job.setErrorCode(errorCode);
        job.setErrorMessage(errorMessage);

        if (job.getAttemptCount() >= job.getMaxAttempts()) {
            // Dead letter
            job.setStatus(ProcessingJobStatus.DEAD_LETTER);
            processingJobRepository.save(job);

            // Update document
            Document document = documentRepository.findById(job.getDocumentId()).orElse(null);
            if (document != null) {
                document.setProcessingStatus(ProcessingStatus.FAILED);
                documentRepository.save(document);
            }
        } else {
            // Schedule retry with backoff
            job.setStatus(ProcessingJobStatus.RETRY_PENDING);
            long backoffMs = calculateBackoff(job.getAttemptCount());
            job.setNextRetryAt(Instant.now().plusMillis(backoffMs));
            processingJobRepository.save(job);
        }
    }

    private long calculateBackoff(int attempt) {
        // Exponential backoff: 2^attempt seconds, capped at 1 hour
        long baseDelaySeconds = 1L << Math.min(attempt, 12); // 2^12 = 4096 seconds (~1 hour)
        return baseDelaySeconds * 1000;
    }
}
