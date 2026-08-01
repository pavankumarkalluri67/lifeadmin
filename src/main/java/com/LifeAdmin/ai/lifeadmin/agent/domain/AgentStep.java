package com.LifeAdmin.ai.lifeadmin.agent.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * AgentStep: records a single step during agent execution.
 * Requirement 25
 */
@Entity
@Table(name = "agent_steps", indexes = {
    @Index(name = "idx_agent_steps_agent_execution_id", columnList = "agent_execution_id")
})
public class AgentStep extends BaseEntity {

    @NotNull
    @Column(name = "agent_execution_id", nullable = false)
    private UUID agentExecutionId;

    @NotNull
    @Min(1)
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private AgentStepType stepType;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgentStepStatus status;

    @Column(name = "tool_name")
    private String toolName;

    @Column(name = "input_summary", columnDefinition = "TEXT")
    private String inputSummary;

    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;

    // Constructors
    public AgentStep() {}

    public AgentStep(UUID agentExecutionId, Integer stepNumber, AgentStepType stepType,
                    String name, AgentStepStatus status) {
        this.agentExecutionId = agentExecutionId;
        this.stepNumber = stepNumber;
        this.stepType = stepType;
        this.name = name;
        this.status = status;
    }

    // Getters and setters
    public UUID getAgentExecutionId() {
        return agentExecutionId;
    }

    public void setAgentExecutionId(UUID agentExecutionId) {
        this.agentExecutionId = agentExecutionId;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public AgentStepType getStepType() {
        return stepType;
    }

    public void setStepType(AgentStepType stepType) {
        this.stepType = stepType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AgentStepStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStepStatus status) {
        this.status = status;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getInputSummary() {
        return inputSummary;
    }

    public void setInputSummary(String inputSummary) {
        this.inputSummary = inputSummary;
    }

    public String getOutputSummary() {
        return outputSummary;
    }

    public void setOutputSummary(String outputSummary) {
        this.outputSummary = outputSummary;
    }
}
