package com.LifeAdmin.ai.lifeadmin.agent.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

/**
 * AgentExecution: tracks execution of an AI agent for a user query.
 * Requirement 24, 25
 */
@Entity
@Table(name = "agent_executions", indexes = {
    @Index(name = "idx_agent_executions_user_id", columnList = "user_id")
})
public class AgentExecution extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgentExecutionStatus status;

    @Column(name = "model_provider")
    private String modelProvider;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Size(max = 64)
    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Constructors
    public AgentExecution() {}

    public AgentExecution(UUID userId) {
        this.userId = userId;
        this.status = AgentExecutionStatus.RUNNING;
    }

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AgentExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(AgentExecutionStatus status) {
        this.status = status;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
