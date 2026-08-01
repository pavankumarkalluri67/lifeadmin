package com.LifeAdmin.ai.lifeadmin.ai.domain;

import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An audit record of a single AI (LLM) invocation.
 *
 * <p>Maps to the {@code ai_invocations} table defined in
 * {@code V1__initial_schema.sql}. Inherits {@code id}, {@code version},
 * {@code created_at}, and {@code updated_at} from {@link BaseEntity}.
 *
 * <p>Captures operational metadata about each call &mdash; operation type,
 * provider, model, prompt version, outcome status, latency, and token counts
 * where available (Req 27.1). Failed, timed-out, and rate-limited calls also
 * carry an {@link #errorCode} (Req 27.3).
 *
 * <p><strong>Privacy:</strong> this entity deliberately has no field for the
 * full prompt text. Prompt/response bodies are never persisted here (Req 27.2).
 */
@Setter
@Getter
@Entity
@Table(name = "ai_invocations")
public class AiInvocation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 64)
    private AiOperationType operationType;

    @Column(name = "provider", nullable = false, length = 64)
    private String provider;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    @Column(name = "prompt_version", length = 64)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AiInvocationStatus status;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    protected AiInvocation() {
        // Required by JPA.
    }

    /**
     * Creates a record of a successful invocation with
     * {@link AiInvocationStatus#SUCCESS} (Req 27.1).
     */
    public static AiInvocation success(AiOperationType operationType,
                                       String provider,
                                       String model,
                                       String promptVersion,
                                       long latencyMs,
                                       Integer inputTokens,
                                       Integer outputTokens) {
        AiInvocation invocation = new AiInvocation();
        invocation.operationType = operationType;
        invocation.provider = provider;
        invocation.model = model;
        invocation.promptVersion = promptVersion;
        invocation.status = AiInvocationStatus.SUCCESS;
        invocation.latencyMs = latencyMs;
        invocation.inputTokens = inputTokens;
        invocation.outputTokens = outputTokens;
        return invocation;
    }

    /**
     * Creates a record of a non-successful invocation carrying an error code
     * (Req 27.3). The {@code status} must be a failure status.
     */
    public static AiInvocation failure(AiOperationType operationType,
                                       String provider,
                                       String model,
                                       String promptVersion,
                                       long latencyMs,
                                       AiInvocationStatus status,
                                       String errorCode) {
        if (status == null || status == AiInvocationStatus.SUCCESS) {
            throw new IllegalArgumentException(
                    "failure requires a failure status (FAILED, TIMEOUT, or RATE_LIMITED)");
        }
        AiInvocation invocation = new AiInvocation();
        invocation.operationType = operationType;
        invocation.provider = provider;
        invocation.model = model;
        invocation.promptVersion = promptVersion;
        invocation.status = status;
        invocation.latencyMs = latencyMs;
        invocation.errorCode = errorCode;
        return invocation;
    }

}
