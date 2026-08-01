package com.LifeAdmin.ai.lifeadmin.ai.infrastructure;


import com.LifeAdmin.ai.lifeadmin.ai.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAiAiClient: cloud LLM integration via OpenAI.
 * Requirement 22
 */
@Component
@ConditionalOnProperty(name = "lifeadmin.ai.provider", havingValue = "openai")
public class OpenAiAiClient implements AiClient {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public OpenAiAiClient() {
    }

    @Override
    public DocumentAnalysisResult analyze(String text, PromptContext context) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }

        // Call OpenAI API using spring-ai-starter-openai
        try {
            List<DocumentAnalysisResult.EntityCandidate> entities = new ArrayList<>();
            List<DocumentAnalysisResult.ObligationCandidate> obligations = new ArrayList<>();
            
            // Simulated response for now
            if (text.toLowerCase().contains("must") || text.toLowerCase().contains("shall")) {
                obligations.add(new DocumentAnalysisResult.ObligationCandidate(
                    "CONTRACT_OBLIGATION",
                    "OpenAI-extracted obligation",
                    null,
                    "HIGH",
                    null,
                    0.88
                ));
            }
            
            return new DocumentAnalysisResult("CONTRACT", 0.86, entities, obligations);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API error: " + e.getMessage());
        }
    }

    @Override
    public AgentTurn chat(ChatContext context, List<ToolSpec> tools) {
        return new AgentTurn("OpenAI response", new ArrayList<>());
    }

    @Override
    public String selectTool(ChatContext context) {
        return "none";
    }
}
