package com.LifeAdmin.ai.lifeadmin.processing.domain;

/**
 * Processing stages tracked through the lifecycle of document processing.
 * Requirement 11
 */
public enum ProcessingStage {
    QUEUED,
    TEXT_EXTRACTION,
    TEXT_NORMALIZATION,
    AI_ANALYSIS,
    ENTITY_PERSISTENCE,
    OBLIGATION_PERSISTENCE,
    COMPLETED
}
