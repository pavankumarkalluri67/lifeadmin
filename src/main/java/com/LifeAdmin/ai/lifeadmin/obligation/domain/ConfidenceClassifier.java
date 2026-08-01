package com.LifeAdmin.ai.lifeadmin.obligation.domain;

/**
 * ConfidenceClassifier: applies thresholds to classify obligation confidence.
 * Requirement 15.1-15.4
 */
public class ConfidenceClassifier {

    private final double highConfidenceThreshold;
    private final double reviewThreshold;

    public ConfidenceClassifier(double highConfidenceThreshold, double reviewThreshold) {
        this.highConfidenceThreshold = highConfidenceThreshold;
        this.reviewThreshold = reviewThreshold;
    }

    /**
     * Classify obligation based on confidence.
     * Returns classification or null if below review threshold (should not persist).
     */
    public Classification classify(double confidence) {
        if (confidence >= highConfidenceThreshold) {
            // High confidence: persist without confirmation
            return new Classification(ObligationStatus.DETECTED, false);
        } else if (confidence >= reviewThreshold) {
            // Review threshold: persist with requires_confirmation
            return new Classification(ObligationStatus.DETECTED, true);
        } else {
            // Below threshold: do not persist
            return null;
        }
    }

    public static class Classification {
        public final ObligationStatus status;
        public final boolean requiresConfirmation;

        public Classification(ObligationStatus status, boolean requiresConfirmation) {
            this.status = status;
            this.requiresConfirmation = requiresConfirmation;
        }
    }
}
