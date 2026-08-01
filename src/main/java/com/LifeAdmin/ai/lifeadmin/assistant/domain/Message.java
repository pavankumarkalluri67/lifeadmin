package com.LifeAdmin.ai.lifeadmin.assistant.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * Message: a message in a conversation.
 * Requirement 23
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_messages_agent_execution_id", columnList = "agent_execution_id")
})
public class Message extends BaseEntity {

    @NotNull
    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "agent_execution_id")
    private UUID agentExecutionId;

    // Constructors
    public Message() {}

    public Message(UUID conversationId, MessageRole role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
    }

    // Getters and setters
    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getAgentExecutionId() {
        return agentExecutionId;
    }

    public void setAgentExecutionId(UUID agentExecutionId) {
        this.agentExecutionId = agentExecutionId;
    }
}
