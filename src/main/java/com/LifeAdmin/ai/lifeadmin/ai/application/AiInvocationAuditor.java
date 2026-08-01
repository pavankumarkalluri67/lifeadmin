package com.LifeAdmin.ai.lifeadmin.ai.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.LifeAdmin.ai.lifeadmin.ai.domain.AiInvocation;
import com.LifeAdmin.ai.lifeadmin.ai.domain.AiInvocationStatus;
import com.LifeAdmin.ai.lifeadmin.ai.domain.AiOperationType;
import com.LifeAdmin.ai.lifeadmin.ai.repository.AiInvocationRepository;

/**
 * Records an audit trail of AI (LLM) invocations into the {@code ai_invocations}
 * table (Req 27).
 *
 * <p>Each recorded entry captures operation type, provider, model, prompt
 * version, outcome status, latency, and token counts where available (Req 27.1).
 * Failed, timed-out, and rate-limited calls additionally carry an error code
 * (Req 27.3).
 *
 * <p><strong>Privacy:</strong> the auditor never receives or persists full
 * prompt/response text; only operational metadata is stored (Req 27.2).
 *
 * <p><strong>Resilience:</strong> auditing is a secondary concern and must never
 * break the primary AI flow. Persistence failures are caught and logged rather
 * than propagated to callers.
 */
@Service
public class AiInvocationAuditor {

    private static final Logger log = LoggerFactory.getLogger(AiInvocationAuditor.class);

    private final AiInvocationRepository repository;

    public AiInvocationAuditor(AiInvocationRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a successful AI invocation with {@link AiInvocationStatus#SUCCESS}
     * (Req 27.1).
     *
     * @param operationType the kind of AI operation performed
     * @param provider      the AI provider identifier (e.g. {@code ollama})
     * @param model         the model name used
     * @param promptVersion the prompt template version, or {@code null} if none
     * @param latencyMs      the end-to-end call latency in milliseconds
     * @param inputTokens    input token count when available, otherwise {@code null}
     * @param outputTokens   output token count when available, otherwise {@code null}
     */
    public void recordSuccess(AiOperationType operationType,
                              String provider,
                              String model,
                              String promptVersion,
                              long latencyMs,
                              Integer inputTokens,
                              Integer outputTokens) {
        save(AiInvocation.success(operationType, provider, model, promptVersion,
                latencyMs, inputTokens, outputTokens));
    }

    /**
     * Records a non-successful AI invocation (Req 27.3).
     *
     * @param operationType the kind of AI operation attempted
     * @param provider      the AI provider identifier
     * @param model         the model name used
     * @param promptVersion the prompt template version, or {@code null} if none
     * @param latencyMs      the elapsed time before failure in milliseconds
     * @param status         one of {@link AiInvocationStatus#FAILED},
     *                       {@link AiInvocationStatus#TIMEOUT}, or
     *                       {@link AiInvocationStatus#RATE_LIMITED}
     * @param errorCode      a short machine-readable error code
     * @throws IllegalArgumentException if {@code status} is {@code SUCCESS}
     */
    public void recordFailure(AiOperationType operationType,
                              String provider,
                              String model,
                              String promptVersion,
                              long latencyMs,
                              AiInvocationStatus status,
                              String errorCode) {
        save(AiInvocation.failure(operationType, provider, model, promptVersion,
                latencyMs, status, errorCode));
    }

    private void save(AiInvocation invocation) {
        try {
            repository.save(invocation);
        } catch (RuntimeException ex) {
            // Auditing must never break the primary AI flow (Req 27).
            log.warn("Failed to persist ai_invocations audit record (operation={}, provider={}, status={})",
                    invocation.getOperationType(), invocation.getProvider(), invocation.getStatus(), ex);
        }
    }
}
