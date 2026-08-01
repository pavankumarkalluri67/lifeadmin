package com.LifeAdmin.ai.lifeadmin.document.domain;

/**
 * Document processing status states.
 * Tracks the lifecycle of a document from upload to completion.
 * Requirement 7.1, 8.3, 11.3
 */
public enum ProcessingStatus {
    UPLOADED,    // Document uploaded, processing not started
    QUEUED,      // Processing job queued
    PROCESSING,  // Processing in progress
    PROCESSED,   // Processing completed successfully
    FAILED       // Processing failed permanently
}
