package com.LifeAdmin.ai.lifeadmin.ai.domain;

import java.util.List;
import java.util.UUID;

/**
 * Minimal, provider-agnostic context passed to {@link AiClient#chat} for a
 * tool-calling assistant turn.
 *
 * <p>Placeholder for task 8.1. The authenticated {@code userId} is always the
 * server-side identity; any LLM-supplied identity is ignored downstream
 * (Req 24.4, 24.5, 26). This record will be expanded by the agent tasks
 * (task 17) and contains no provider SDK types (Req 22.5).
 *
 * @param userId  the server-side authenticated user identity
 * @param message the latest user message driving this turn
 * @param history prior to turn messages, oldest first (maybe empty)
 */
public record ChatContext(UUID userId, String message, List<ChatMessage> history) {

    /**
     * A single message in the conversation history.
     *
     * @param role    the role of the author (for example {@code USER} or {@code ASSISTANT})
     * @param content the message content
     */
    public record ChatMessage(String role, String content) {
    }
}
