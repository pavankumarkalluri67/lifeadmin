package com.LifeAdmin.ai.lifeadmin.assistant.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * Conversation: a conversation between user and AI assistant.
 * Requirement 23
 */
@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conversations_user_id", columnList = "user_id")
})
public class Conversation extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConversationStatus status;

    // Constructors
    public Conversation() {}

    public Conversation(UUID userId) {
        this.userId = userId;
        this.status = ConversationStatus.ACTIVE;
    }

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }
}
