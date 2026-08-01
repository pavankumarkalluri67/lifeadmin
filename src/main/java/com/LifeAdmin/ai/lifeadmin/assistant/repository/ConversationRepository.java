package com.LifeAdmin.ai.lifeadmin.assistant.repository;


import com.LifeAdmin.ai.lifeadmin.assistant.domain.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByUserId(UUID userId, Pageable pageable);
    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
}
