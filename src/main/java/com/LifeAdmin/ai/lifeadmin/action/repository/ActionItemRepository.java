package com.LifeAdmin.ai.lifeadmin.action.repository;


import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItem;
import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, UUID> {
    Page<ActionItem> findByUserId(UUID userId, Pageable pageable);
    Optional<ActionItem> findByIdAndUserId(UUID id, UUID userId);
    
    @Query("SELECT COUNT(a) FROM ActionItem a WHERE a.userId = :userId AND a.status = :status")
    long countByUserIdAndStatus(UUID userId, ActionItemStatus status);
}
