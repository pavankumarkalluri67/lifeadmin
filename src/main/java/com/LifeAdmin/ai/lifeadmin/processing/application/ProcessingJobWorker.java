package com.LifeAdmin.ai.lifeadmin.processing.application;

import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJob;
import com.LifeAdmin.ai.lifeadmin.processing.domain.ProcessingJobStatus;
import com.LifeAdmin.ai.lifeadmin.processing.repository.ProcessingJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Polls for queued work and runs the document pipeline (Req 11, 16).
 */
@Component
public class ProcessingJobWorker {

    private static final Logger log = LoggerFactory.getLogger(ProcessingJobWorker.class);

    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingOrchestrator processingOrchestrator;

    public ProcessingJobWorker(ProcessingJobRepository processingJobRepository,
                               ProcessingOrchestrator processingOrchestrator) {
        this.processingJobRepository = processingJobRepository;
        this.processingOrchestrator = processingOrchestrator;
    }

    @Scheduled(fixedDelayString = "${lifeadmin.processing.poll-interval-ms:5000}")
    public void pollAndExecute() {
        for (ProcessingJob job : dueJobs()) {
            try {
                processingOrchestrator.execute(job.getId());
            } catch (Exception e) {
                log.error("Processing job {} failed", job.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    protected List<ProcessingJob> dueJobs() {
        Instant now = Instant.now();
        return processingJobRepository
                .findByStatusIn(List.of(ProcessingJobStatus.PENDING, ProcessingJobStatus.RETRY_PENDING))
                .stream()
                .filter(job -> job.getStatus() == ProcessingJobStatus.PENDING
                        || job.getNextRetryAt() == null
                        || !job.getNextRetryAt().isAfter(now))
                .toList();
    }
}
