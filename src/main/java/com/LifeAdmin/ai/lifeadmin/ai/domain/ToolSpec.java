package com.LifeAdmin.ai.lifeadmin.ai.domain;

/**
 * Minimal, provider-agnostic description of a tool the assistant may call
 * during a {@link AiClient#chat} turn.
 *
 * <p>Placeholder for task 8.1. The concrete tool set (the seven Java tools) and
 * their execution are implemented in the agent tasks (task 17). The parameter
 * schema is expressed as a provider-neutral JSON Schema string so the domain
 * layer stays free of provider SDK types (Req 22.5).
 *
 * @param name             the unique tool name
 * @param description      a natural-language description of what the tool does
 * @param parametersSchema a JSON Schema describing the tool's input parameters
 */
public record ToolSpec(String name, String description, String parametersSchema) {
}
