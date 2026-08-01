package com.LifeAdmin.ai.lifeadmin.processing.application;


import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJobStatus;
import com.LifeAdmin.ai.lifeadmin.processing.repository.ProcessingJobRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * JobRecoveryService: recovers jobs left in intermediate states on startup.
 * Requirement 11, 16
 *
 * On application startup, this service:
 * 1. Finds all jobs with status RUNNING (crashed mid-processing)
 * 2. Finds all jobs with status RETRY_PENDING and nextRetryAt <= now
 * 3. Resets RUNNING jobs to PENDING
 * 4. Submits RETRY_PENDING jobs to the processing queue
 */
@Service
public class JobRecoveryService {

    private final ProcessingJobRepository processingJobRepository;

    public JobRecoveryService(ProcessingJobRepository processingJobRepository,
                            ProcessingOrchestrator processingOrchestrator) {
        this.processingJobRepository = processingJobRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverJobs() {
        // Recover RUNNING jobs (crashed mid-processing)
        List<ProcessingJob> runningJobs = processingJobRepository.findByStatus(ProcessingJobStatus.RUNNING);
        for (ProcessingJob job : runningJobs) {
            job.setStatus(ProcessingJobStatus.PENDING);
            processingJobRepository.save(job);
        }

        // Recover RETRY_PENDING jobs that are ready for retry
        List<ProcessingJob> retryJobs = processingJobRepository.findByStatus(ProcessingJobStatus.RETRY_PENDING);
        Instant now = Instant.now();
        for (ProcessingJob job : retryJobs) {
            if (job.getNextRetryAt() != null && job.getNextRetryAt().isBefore(now)) {
                job.setStatus(ProcessingJobStatus.PENDING);
                processingJobRepository.save(job);
            }
        }
    }
}
