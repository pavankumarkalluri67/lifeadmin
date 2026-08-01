package com.LifeAdmin.ai.lifeadmin.processing.domain;

/**
 * Processing job status states.
 * Requirement 11, 16
 */
public enum ProcessingJobStatus {
    PENDING,           // Job created, not yet started
    RUNNING,           // Job currently executing
    COMPLETED,         // Job completed successfully
    FAILED,            // Job failed permanently
    RETRY_PENDING,     // Job failed transiently, scheduled for retry
    DEAD_LETTER        // Job exhausted all retries
}
