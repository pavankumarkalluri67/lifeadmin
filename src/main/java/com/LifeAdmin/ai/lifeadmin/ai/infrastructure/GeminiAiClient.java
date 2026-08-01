package com.LifeAdmin.ai.lifeadmin.ai.infrastructure;


import com.LifeAdmin.ai.lifeadmin.ai.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * GeminiAiClient: cloud LLM integration via Google Gemini.
 * Requirement 22
 */
@Component
@ConditionalOnProperty(name = "lifeadmin.ai.provider", havingValue = "gemini")
public class GeminiAiClient implements AiClient {

    @Value("${lifeadmin.ai.gemini.api-key:}")
    private String apiKey;

    public GeminiAiClient() {
    }

    @Override
    public DocumentAnalysisResult analyze(String text, PromptContext context) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key not configured");
        }

        try {
            List<DocumentAnalysisResult.EntityCandidate> entities = new ArrayList<>();
            List<DocumentAnalysisResult.ObligationCandidate> obligations = new ArrayList<>();
            
            // Simulated response for now
            if (text.toLowerCase().contains("deadline") || text.toLowerCase().contains("date")) {
                entities.add(new DocumentAnalysisResult.EntityCandidate(
                    "DATE",
                    "2026-12-31",
                    "2026-12-31",
                    0.92
                ));
            }
            
            return new DocumentAnalysisResult("CONTRACT", 0.85, entities, obligations);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API error: " + e.getMessage());
        }
    }

    @Override
    public AgentTurn chat(ChatContext context, List<ToolSpec> tools) {
        return new AgentTurn("Gemini response", new ArrayList<>());
    }

    @Override
    public String selectTool(ChatContext context) {
        return "none";
    }
}
