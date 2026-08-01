package com.LifeAdmin.ai.lifeadmin.obligation.repository;


import com.LifeAdmin.ai.lifeadmin.obligation.domain.Obligation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObligationRepository extends JpaRepository<Obligation, UUID> {
    Page<Obligation> findByUserId(UUID userId, Pageable pageable);
    Optional<Obligation> findByIdAndUserId(UUID id, UUID userId);
    Optional<Obligation> findByDocumentIdAndTypeAndNormalizedDueDateAndNormalizedReference(
        UUID documentId, String type, Object normalizedDueDate, String normalizedReference);
    
    @Query("SELECT COUNT(o) FROM Obligation o WHERE o.userId = :userId AND o.dueDate >= CURRENT_DATE")
    long countUpcoming(UUID userId);
    
    @Query("SELECT COUNT(o) FROM Obligation o WHERE o.userId = :userId AND o.requiresConfirmation = true")
    long countRequiringConfirmation(UUID userId);
}
