package com.LifeAdmin.ai.lifeadmin.obligation.domain;

/**
 * Obligation status states.
 * Requirement 15, 18
 */
public enum ObligationStatus {
    DETECTED,     // Detected from document or created by user
    CONFIRMED,    // User confirmed the obligation
    DISMISSED,    // User dismissed the obligation
    COMPLETED     // Obligation completed
}
