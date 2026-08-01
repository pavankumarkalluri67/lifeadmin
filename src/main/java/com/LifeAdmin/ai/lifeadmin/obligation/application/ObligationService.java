package com.LifeAdmin.ai.lifeadmin.obligation.application;



import com.LifeAdmin.ai.lifeadmin.ai.domain.DocumentAnalysisResult;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.*;
import com.LifeAdmin.ai.lifeadmin.obligation.repository.ObligationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Obligation_Service: obligation detection with idempotency and management.
 * Requirement 15, 17, 18
 */
@Service
public class ObligationService {

    private final ObligationRepository obligationRepository;
    private final CurrentUserProvider currentUserProvider;
    
    @Value("${lifeadmin.obligation.high-confidence-threshold:0.90}")
    private double highConfidenceThreshold;

    @Value("${lifeadmin.obligation.review-threshold:0.70}")
    private double reviewThreshold;

    @Value("${lifeadmin.obligation.default-priority:MEDIUM}")
    private String defaultPriority;

    public ObligationService(ObligationRepository obligationRepository,
                            CurrentUserProvider currentUserProvider) {
        this.obligationRepository = obligationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void createFromAnalysis(UUID userId, UUID documentId, DocumentAnalysisResult result) {
        ConfidenceClassifier classifier = new ConfidenceClassifier(highConfidenceThreshold, reviewThreshold);

        if (result.obligations() == null || result.obligations().isEmpty()) {
            return;
        }

        for (DocumentAnalysisResult.ObligationCandidate oblCandidate : result.obligations()) {
            // Classify by confidence
            ConfidenceClassifier.Classification classification = classifier.classify(oblCandidate.confidence());
            
            if (classification == null) {
                // Below threshold - audit only, do not persist
                // TODO: Emit audit log entry
                continue;
            }

            // Compute dedup identity
            LocalDate normalizedDueDate = oblCandidate.dueDate() != null
                ? DedupIdentity.normalizeDate(oblCandidate.dueDate())
                : null;
            String normalizedReference = DedupIdentity.normalizeReference(oblCandidate.reference());

            // Check for existing obligation (dedup)
            if (normalizedDueDate != null && documentId != null) {
                var existing = obligationRepository.findByDocumentIdAndTypeAndNormalizedDueDateAndNormalizedReference(
                    documentId, oblCandidate.type(), normalizedDueDate, normalizedReference
                );
                if (existing.isPresent()) {
                    // Already exists, treat as successful dedup
                    continue;
                }
            }

            // Create obligation
            Obligation obligation = new Obligation(
                userId,
                documentId,
                oblCandidate.type(),
                oblCandidate.title(),
                Priority.valueOf(oblCandidate.priority() != null ? oblCandidate.priority() : defaultPriority),
                oblCandidate.confidence(),
                SourceType.AI
            );

            obligation.setDueDate(oblCandidate.dueDate());
            obligation.setStatus(classification.status);
            obligation.setRequiresConfirmation(classification.requiresConfirmation);
            obligation.setNormalizedDueDate(normalizedDueDate);
            obligation.setNormalizedReference(normalizedReference);

            try {
                obligationRepository.save(obligation);
            } catch (Exception e) {
                // Likely a unique constraint violation on dedup identity
                // Treat as successful dedup (do not propagate error)
                if (e.getCause() != null && e.getCause().getMessage().contains("uq_obligations_dedup_identity")) {
                    // Dedup success - silently continue
                    continue;
                }
                throw e;
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Obligation> list(Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        return obligationRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Obligation get(UUID obligationId) {
        UUID userId = currentUserProvider.getUserId();
        return obligationRepository.findByIdAndUserId(obligationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Obligation not found"));
    }

    @Transactional
    public Obligation confirm(UUID obligationId) {
        Obligation obligation = get(obligationId);
        obligation.setStatus(ObligationStatus.CONFIRMED);
        obligation.setConfirmedAt(Instant.now());
        obligation.setRequiresConfirmation(false);
        return obligationRepository.save(obligation);
    }

    @Transactional
    public Obligation dismiss(UUID obligationId) {
        Obligation obligation = get(obligationId);
        obligation.setStatus(ObligationStatus.DISMISSED);
        obligation.setDismissedAt(Instant.now());
        return obligationRepository.save(obligation);
    }

    @Transactional
    public Obligation complete(UUID obligationId) {
        Obligation obligation = get(obligationId);
        obligation.setStatus(ObligationStatus.COMPLETED);
        obligation.setCompletedAt(Instant.now());
        return obligationRepository.save(obligation);
    }

    @Transactional
    public Obligation update(UUID obligationId, String title, LocalDate dueDate, Priority priority) {
        Obligation obligation = get(obligationId);
        if (title != null && !title.isBlank()) {
            obligation.setTitle(title);
        }
        if (dueDate != null) {
            obligation.setDueDate(dueDate);
            obligation.setNormalizedDueDate(DedupIdentity.normalizeDate(dueDate));
        }
        if (priority != null) {
            obligation.setPriority(priority);
        }
        return obligationRepository.save(obligation);
    }
}
