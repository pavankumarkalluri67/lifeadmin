package com.LifeAdmin.ai.lifeadmin.ai.domain;

import java.util.List;

/**
 * Minimal, provider-agnostic result of a single tool-calling assistant turn
 * returned by {@link AiClient#chat}.
 *
 * <p>Placeholder for task 8.1. When {@code toolCalls} is non-empty the caller
 * (the agent orchestrator, task 17) is expected to execute the requested tools
 * in Java and continue the loop; otherwise {@code content} is the assistant's
 * final answer. It contains no provider SDK types (Req 22.5).
 *
 * @param content   the assistant's message content for this turn (may be blank
 *                  when the turn only requests tool calls)
 * @param toolCalls tools the assistant requested to invoke this turn
 */
public record AgentTurn(String content, List<ToolCall> toolCalls) {

    /**
     * A request from the assistant to invoke a named tool with JSON arguments.
     *
     * @param toolName      the name of the tool to invoke (matches a {@link ToolSpec#name()})
     * @param argumentsJson the tool arguments encoded as a JSON object string
     */
    public record ToolCall(String toolName, String argumentsJson) {
    }

    /**
     * @return {@code true} when the assistant requested one or more tool calls
     */
    public boolean requiresToolExecution() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
