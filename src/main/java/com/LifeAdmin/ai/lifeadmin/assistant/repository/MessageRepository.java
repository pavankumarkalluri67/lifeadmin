package com.LifeAdmin.ai.lifeadmin.assistant.repository;

import com.LifeAdmin.ai.lifeadmin.assistant.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversationId(UUID conversationId, Pageable pageable);
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
