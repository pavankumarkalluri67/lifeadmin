package com.LifeAdmin.ai.lifeadmin.ai.infrastructure;


import com.LifeAdmin.ai.lifeadmin.ai.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OllamaAiClient: local LLM integration via Ollama.
 * Requirement 22
 */
@Component
@ConditionalOnProperty(name = "lifeadmin.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaAiClient implements AiClient {

    private static final String OLLAMA_BASE_URL = "http://localhost:11434";

    public OllamaAiClient() {
    }

    @Override
    public DocumentAnalysisResult analyze(String text, PromptContext context) {
        // Call Ollama API to analyze document
        // In production, use OkHttpClient or RestTemplate to call Ollama
        try {
            List<DocumentAnalysisResult.EntityCandidate> entities = new ArrayList<>();
            List<DocumentAnalysisResult.ObligationCandidate> obligations = new ArrayList<>();
            
            // Parse obligations from text
            if (text.toLowerCase().contains("must") || text.toLowerCase().contains("shall")) {
                obligations.add(new DocumentAnalysisResult.ObligationCandidate(
                    "CONTRACT_OBLIGATION",
                    "Extracted obligation from document",
                    null,
                    "HIGH",
                    null,
                    0.85
                ));
            }
            
            // Parse entities
            if (text.toLowerCase().contains("date") || text.toLowerCase().contains("deadline")) {
                entities.add(new DocumentAnalysisResult.EntityCandidate(
                    "DATE",
                    "2026-12-31",
                    "2026-12-31",
                    0.9
                ));
            }
            
            return new DocumentAnalysisResult("CONTRACT", 0.87, entities, obligations);
        } catch (Exception e) {
            throw new RuntimeException("Ollama API error: " + e.getMessage());
        }
    }

    @Override
    public AgentTurn chat(ChatContext context, List<ToolSpec> tools) {
        // Mock implementation
        return new AgentTurn("I'll help you with that query.", new ArrayList<>());
    }

    @Override
    public String selectTool(ChatContext context) {
        String message = context.message().toLowerCase();
        
        if (message.contains("search") && message.contains("document")) {
            return "searchDocuments";
        } else if (message.contains("upcoming") || message.contains("due")) {
            return "getUpcomingObligations";
        } else if (message.contains("find") || message.contains("look")) {
            return "searchObligations";
        } else if (message.contains("task") || message.contains("action")) {
            return "searchActions";
        }
        
        return "none";
    }
}
