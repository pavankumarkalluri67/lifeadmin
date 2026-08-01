package com.LifeAdmin.ai.lifeadmin.reminder.repository;


import com.LifeAdmin.ai.lifeadmin.reminder.domain.Reminder;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.ReminderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    Page<Reminder> findByUserId(UUID userId, Pageable pageable);
    Optional<Reminder> findByIdAndUserId(UUID id, UUID userId);
    List<Reminder> findByStatusAndRemindAtLessThanEqual(ReminderStatus status, Instant now);
    
    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.userId = :userId AND r.status = :status")
    long countByUserIdAndStatus(UUID userId, ReminderStatus status);
}
