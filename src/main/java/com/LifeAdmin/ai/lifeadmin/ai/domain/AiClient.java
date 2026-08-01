package com.LifeAdmin.ai.lifeadmin.ai.domain;

import java.util.List;

/**
 * Provider-agnostic port for LLM access (Req 22.3).
 *
 * <p>This is the single abstraction the application and domain layers use to
 * reach an AI_Provider. Implementations (Ollama, OpenAI, Gemini) live in
 * {@code ai.infrastructure} and are selected by the {@code AI_PROVIDER}
 * configuration value (task 8.2). No provider SDK type may appear on this port;
 * the domain layer MUST NOT depend on any model provider SDK (Req 22.5).
 *
 * <p>Both capabilities are exposed regardless of the selected provider
 * (Req 22.3). When the configured provider is unreachable, implementations
 * surface a provider-unavailable error so dependent flows can apply their retry
 * or degradation rules (Req 22.4).
 */
public interface AiClient {

    /**
     * Produce structured document analysis output for the given text (Req 13.1).
     *
     * <p>The returned {@link DocumentAnalysisResult} is transient and MUST be
     * validated (see {@link DocumentAnalysisValidator}) before any persistence
     * occurs (Req 13.2).
     *
     * @param text    the extracted, normalized document text to analyze
     * @param context provider-agnostic prompt context (for example prompt version)
     * @return the structured analysis result
     */
    DocumentAnalysisResult analyze(String text, PromptContext context);

    /**
     * Execute a single tool-calling assistant turn (Req 22.3, 24).
     *
     * @param context provider-agnostic chat context bound to the server-side user
     * @param tools   the tools the assistant is permitted to call this turn
     * @return the assistant's turn, either a final answer or requested tool calls
     */
    AgentTurn chat(ChatContext context, List<ToolSpec> tools);

    /**
     * Select and execute a tool based on user query (Req 24).
     *
     * @param context the chat context with user message
     * @return the tool name to execute, or "none" if no tool should be called
     */
    String selectTool(ChatContext context);
}
