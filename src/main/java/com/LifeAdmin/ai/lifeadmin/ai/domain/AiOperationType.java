package com.LifeAdmin.ai.lifeadmin.ai.domain;

/**
 * The kind of AI operation being invoked against the LLM.
 *
 * <p>Stored as a string in the {@code ai_invocations.operation_type} column
 * (see {@code @Enumerated(EnumType.STRING)}). Used to distinguish structured
 * document analysis calls from conversational agent turns when auditing AI
 * usage (Req 27.1).
 */
public enum AiOperationType {
    DOCUMENT_ANALYSIS,
    AGENT_CHAT
}
