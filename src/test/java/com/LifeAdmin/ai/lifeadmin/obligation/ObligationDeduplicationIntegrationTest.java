package com.LifeAdmin.ai.lifeadmin.obligation;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ObligationDeduplicationIntegrationTest {

    @Autowired
    private com.lifeadmin.obligation.application.ObligationService obligationService;

    @Autowired
    private ObligationRepository obligationRepository;

    @Test
    public void testObligationDedup_SameIdentity_NotCreatedTwice() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDate dueDate = LocalDate.of(2026, 12, 31);
        
        DocumentAnalysisResult result = new DocumentAnalysisResult();
        DocumentAnalysisResult.ObligationData obl = new DocumentAnalysisResult.ObligationData();
        obl.setType("CONTRACT_OBLIGATION");
        obl.setTitle("Payment Due");
        obl.setDueDate(dueDate);
        obl.setConfidence(0.95);
        obl.setPriority("HIGH");
        result.addObligation(obl);

        // Act - First creation
        obligationService.createFromAnalysis(userId, documentId, result);
        
        // Act - Second creation (same identity)
        obligationService.createFromAnalysis(userId, documentId, result);

        // Assert - Only one obligation created
        long count = obligationRepository.findByUserId(userId, 
            org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements();
        assertEquals(1, count, "Dedup should prevent duplicate obligations");
    }

    @Test
    public void testHighConfidenceObligation_NoConfirmationRequired() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        DocumentAnalysisResult result = new DocumentAnalysisResult();
        DocumentAnalysisResult.ObligationData obl = new DocumentAnalysisResult.ObligationData();
        obl.setType("CONTRACT_OBLIGATION");
        obl.setTitle("High Confidence Task");
        obl.setDueDate(LocalDate.of(2026, 12, 31));
        obl.setConfidence(0.95);  // >= 0.90 (high confidence)
        obl.setPriority("HIGH");
        result.addObligation(obl);

        // Act
        obligationService.createFromAnalysis(userId, documentId, result);

        // Assert
        var obligations = obligationRepository.findByUserId(userId,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, obligations.getTotalElements());
        assertFalse(obligations.getContent().get(0).isRequiresConfirmation());
    }

    @Test
    public void testReviewConfidenceObligation_ConfirmationRequired() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        DocumentAnalysisResult result = new DocumentAnalysisResult();
        DocumentAnalysisResult.ObligationData obl = new DocumentAnalysisResult.ObligationData();
        obl.setType("CONTRACT_OBLIGATION");
        obl.setTitle("Review Threshold Task");
        obl.setDueDate(LocalDate.of(2026, 12, 31));
        obl.setConfidence(0.80);  // >= 0.70, < 0.90 (review threshold)
        obl.setPriority("MEDIUM");
        result.addObligation(obl);

        // Act
        obligationService.createFromAnalysis(userId, documentId, result);

        // Assert
        var obligations = obligationRepository.findByUserId(userId,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, obligations.getTotalElements());
        assertTrue(obligations.getContent().get(0).isRequiresConfirmation());
    }

    @Test
    public void testLowConfidenceObligation_NotPersisted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        
        DocumentAnalysisResult result = new DocumentAnalysisResult();
        DocumentAnalysisResult.ObligationData obl = new DocumentAnalysisResult.ObligationData();
        obl.setType("CONTRACT_OBLIGATION");
        obl.setTitle("Low Confidence Task");
        obl.setDueDate(LocalDate.of(2026, 12, 31));
        obl.setConfidence(0.60);  // < 0.70 (below review threshold)
        obl.setPriority("LOW");
        result.addObligation(obl);

        // Act
        obligationService.createFromAnalysis(userId, documentId, result);

        // Assert
        var obligations = obligationRepository.findByUserId(userId,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(0, obligations.getTotalElements(), "Low confidence obligations should not be persisted");
    }
}
