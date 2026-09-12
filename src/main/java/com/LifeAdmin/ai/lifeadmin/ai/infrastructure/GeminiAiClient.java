package com.LifeAdmin.ai.lifeadmin.ai.infrastructure;

import com.LifeAdmin.ai.lifeadmin.ai.domain.AgentTurn;
import com.LifeAdmin.ai.lifeadmin.ai.domain.AiClient;
import com.LifeAdmin.ai.lifeadmin.ai.domain.ChatContext;
import com.LifeAdmin.ai.lifeadmin.ai.domain.DocumentAnalysisResult;
import com.LifeAdmin.ai.lifeadmin.ai.domain.PromptContext;
import com.LifeAdmin.ai.lifeadmin.ai.domain.ToolSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * GeminiAiClient: cloud LLM integration via the Google Generative Language REST API.
 * Requirement 22
 */
@Component
@ConditionalOnProperty(name = "lifeadmin.ai.provider", havingValue = "gemini")
public class GeminiAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    /** Guards against sending an entire large document and blowing the token budget. */
    private static final int MAX_INPUT_CHARS = 60_000;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiClient(ObjectMapper objectMapper,
                          RestClient.Builder restClientBuilder,
                          @Value("${lifeadmin.ai.gemini.api-key:}") String apiKey,
                          @Value("${lifeadmin.ai.gemini.model:gemini-2.5-flash}") String model,
                          @Value("${lifeadmin.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public DocumentAnalysisResult analyze(String text, PromptContext context) {
        requireApiKey();

        ObjectNode request = objectMapper.createObjectNode();
        request.set("systemInstruction", textPart(GeminiPrompts.ANALYSIS_SYSTEM_PROMPT));
        request.set("contents", objectMapper.createArrayNode().add(userContent(truncate(text))));

        ObjectNode generationConfig = request.putObject("generationConfig");
        generationConfig.put("temperature", 0.0);
        generationConfig.put("responseMimeType", "application/json");
        try {
            generationConfig.set("responseSchema", objectMapper.readTree(GeminiPrompts.ANALYSIS_RESPONSE_SCHEMA));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Gemini response schema", e);
        }

        String json = firstTextCandidate(send(request));
        try {
            return objectMapper.readValue(json, DocumentAnalysisResult.class);
        } catch (Exception e) {
            throw new GeminiApiException("Gemini returned unparseable analysis JSON", e);
        }
    }

    @Override
    public AgentTurn chat(ChatContext context, List<ToolSpec> tools) {
        requireApiKey();

        ObjectNode request = objectMapper.createObjectNode();
        request.set("systemInstruction", textPart(GeminiPrompts.CHAT_SYSTEM_PROMPT));

        ArrayNode contents = request.putArray("contents");
        if (context.history() != null) {
            for (ChatContext.ChatMessage message : context.history()) {
                ObjectNode turn = contents.addObject();
                turn.put("role", "ASSISTANT".equalsIgnoreCase(message.role()) ? "model" : "user");
                turn.putArray("parts").addObject().put("text", message.content());
            }
        }
        contents.add(userContent(context.message()));

        if (tools != null && !tools.isEmpty()) {
            ArrayNode declarations = request.putArray("tools").addObject().putArray("functionDeclarations");
            for (ToolSpec tool : tools) {
                ObjectNode declaration = declarations.addObject();
                declaration.put("name", tool.name());
                declaration.put("description", tool.description());
                try {
                    declaration.set("parameters", objectMapper.readTree(tool.parametersSchema()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Tool " + tool.name() + " has an invalid parameter schema", e);
                }
            }
        }

        JsonNode response = send(request);
        StringBuilder content = new StringBuilder();
        List<AgentTurn.ToolCall> toolCalls = new ArrayList<>();
        for (JsonNode part : response.path("candidates").path(0).path("content").path("parts")) {
            if (part.hasNonNull("text")) {
                content.append(part.get("text").asText());
            }
            JsonNode call = part.path("functionCall");
            if (!call.isMissingNode()) {
                toolCalls.add(new AgentTurn.ToolCall(call.path("name").asText(), call.path("args").toString()));
            }
        }
        return new AgentTurn(content.toString(), toolCalls);
    }

    @Override
    public String selectTool(ChatContext context) {
        AgentTurn turn = chat(context, List.of());
        return turn.requiresToolExecution() ? turn.toolCalls().get(0).toolName() : "none";
    }

    private JsonNode send(ObjectNode request) {
        try {
            return restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(request.toString())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("Gemini call failed for model {}", model);
            throw new GeminiApiException("Gemini API call failed", e);
        }
    }

    private String firstTextCandidate(JsonNode response) {
        JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode() || text.asText().isBlank()) {
            throw new GeminiApiException("Gemini returned no usable content", null);
        }
        return text.asText();
    }

    private ObjectNode userContent(String text) {
        ObjectNode content = objectMapper.createObjectNode();
        content.put("role", "user");
        content.putArray("parts").addObject().put("text", text);
        return content;
    }

    private ObjectNode textPart(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.putArray("parts").addObject().put("text", text);
        return node;
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key not configured (set GEMINI_API_KEY)");
        }
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= MAX_INPUT_CHARS ? text : text.substring(0, MAX_INPUT_CHARS);
    }

    /** Signals a provider-side failure so the Processing_Service retry policy applies (Req 16). */
    static class GeminiApiException extends RuntimeException {
        GeminiApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
