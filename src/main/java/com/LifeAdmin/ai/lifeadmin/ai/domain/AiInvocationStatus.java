package com.LifeAdmin.ai.lifeadmin.ai.domain;

/**
 * Outcome status of a single AI (LLM) invocation.
 *
 * <p>Stored as a string in the {@code ai_invocations.status} column
 * (see {@code @Enumerated(EnumType.STRING)}). A successful call records
 * {@link #SUCCESS}; failures record one of {@link #FAILED}, {@link #TIMEOUT},
 * or {@link #RATE_LIMITED} together with an {@code error_code} (Req 27.3).
 */
public enum AiInvocationStatus {
    SUCCESS,
    FAILED,
    TIMEOUT,
    RATE_LIMITED
}
