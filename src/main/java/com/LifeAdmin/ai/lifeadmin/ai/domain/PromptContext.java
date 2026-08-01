package com.LifeAdmin.ai.lifeadmin.ai.domain;

/**
 * Minimal, provider-agnostic context passed to {@link AiClient#analyze} for
 * structured output generation.
 *
 * <p>Placeholder for task 8.1 — carries only the prompt version used for
 * auditing (Req 27) and reproducibility. It will be fleshed out in task 8.2 and
 * the processing/agent tasks. It intentionally contains no provider SDK types
 * so that the domain layer remains free of provider dependencies (Req 22.5).
 *
 * @param promptVersion the version identifier of the prompt template in use
 */
public record PromptContext(String promptVersion) {
}
