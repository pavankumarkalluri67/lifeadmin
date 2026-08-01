package com.LifeAdmin.ai.lifeadmin.agent.application;


import com.LifeAdmin.ai.lifeadmin.agent.domain.*;
import com.LifeAdmin.ai.lifeadmin.agent.repository.AgentExecutionRepository;
import com.LifeAdmin.ai.lifeadmin.agent.repository.AgentStepRepository;
import com.LifeAdmin.ai.lifeadmin.ai.domain.AiClient;
import com.LifeAdmin.ai.lifeadmin.ai.domain.ChatContext;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agent_Orchestrator: orchestrates AI agent execution with tool calling.
 * Implements the 7 tools available to the agent.
 * Requirement 24, 25
 * Key Design: ALL tool execution happens in Java with server-side userId binding.
 * The LLM-supplied user_id is NEVER used; CurrentUserProvider is the source of truth.
 */
@Service
public class AgentOrchestrator {

    private static final int MAX_ITERATIONS = 10;
    private static final String MODEL_PROVIDER = "ollama";  // Will be configurable
    private static final String MODEL_NAME = "llama2";

    private final AiClient aiClient;
    private final AgentTools agentTools;
    private final AgentExecutionRepository agentExecutionRepository;
    private final AgentStepRepository agentStepRepository;
    private final CurrentUserProvider currentUserProvider;

    public AgentOrchestrator(AiClient aiClient,
                            AgentTools agentTools,
                            AgentExecutionRepository agentExecutionRepository,
                            AgentStepRepository agentStepRepository,
                            CurrentUserProvider currentUserProvider) {
        this.aiClient = aiClient;
        this.agentTools = agentTools;
        this.agentExecutionRepository = agentExecutionRepository;
        this.agentStepRepository = agentStepRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public AgentExecutionResponse run(String userQuery) {
        UUID userId = currentUserProvider.getUserId();
        
        // Create agent execution
        AgentExecution execution = new AgentExecution(userId);
        execution.setModelProvider(MODEL_PROVIDER);
        execution.setModelName(MODEL_NAME);
        agentExecutionRepository.save(execution);

        try {
            String finalAnswer = orchestrateTurns(execution, userQuery);
            
            execution.setStatus(AgentExecutionStatus.COMPLETED);
            execution.setCompletedAt(Instant.now());
            agentExecutionRepository.save(execution);
            
            return new AgentExecutionResponse(execution.getId().toString(), finalAnswer, true);
        } catch (Exception e) {
            execution.setStatus(AgentExecutionStatus.FAILED);
            execution.setFailureCode("ORCHESTRATION_ERROR");
            execution.setFailureMessage(e.getMessage());
            execution.setCompletedAt(Instant.now());
            agentExecutionRepository.save(execution);
            
            return new AgentExecutionResponse(execution.getId().toString(), 
                "Error processing query: " + e.getMessage(), false);
        }
    }

    private String orchestrateTurns(AgentExecution execution, String userQuery) {
        List<String> toolCalls = new ArrayList<>();
        String answer = "I couldn't generate an answer for your query.";
        
        for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
            // Create step record
            AgentStep step = new AgentStep(
                execution.getId(),
                iteration,
                AgentStepType.TOOL_CALL,
                "Tool Call " + iteration,
                AgentStepStatus.SUCCESS
            );
            
            try {
                // Build chat context
                ChatContext context = new ChatContext(execution.getUserId(), userQuery, new ArrayList<>());

                // Call LLM for tool selection
                String llmResponse = aiClient.selectTool(context);
                
                if (llmResponse == null || llmResponse.isBlank() || 
                    llmResponse.equalsIgnoreCase("none") || llmResponse.equalsIgnoreCase("final")) {
                    // LLM chose to finish
                    answer = userQuery.length() < 100 ? userQuery : userQuery.substring(0, 100);
                    step.setStepType(AgentStepType.COMPLETION);
                    step.setName("Agent Finished");
                    agentStepRepository.save(step);
                    break;
                }

                // Execute tool based on LLM response
                String toolName = extractToolName(llmResponse);
                step.setToolName(toolName);
                step.setInputSummary(llmResponse);

                String toolResult = executeTool(toolName, llmResponse);
                step.setOutputSummary(toolResult);
                toolCalls.add(toolName + " returned: " + toolResult);
                
                agentStepRepository.save(step);
                
            } catch (Exception e) {
                step.setStatus(AgentStepStatus.FAILED);
                step.setOutputSummary("Error: " + e.getMessage());
                agentStepRepository.save(step);
                
                if (iteration >= MAX_ITERATIONS) {
                    throw new RuntimeException("Max iterations reached");
                }
            }
        }

        return answer;
    }

    private String executeTool(String toolName, String input) {
        // Parse tool name and parameters from LLM response
        String query = extractQueryParameter(input);
        
        return switch (toolName) {
            case "searchDocuments" -> {
                var result = agentTools.searchDocuments(query);
                yield result.toString();
            }
            case "getDocument" -> {
                var result = agentTools.getDocument(query);
                yield result.toString();
            }
            case "searchObligations" -> {
                var result = agentTools.searchObligations(query);
                yield result.toString();
            }
            case "getUpcomingObligations" -> {
                int days = extractDaysParameter(input);
                var result = agentTools.getUpcomingObligations(days);
                yield result.toString();
            }
            case "searchActions" -> {
                var result = agentTools.searchActions(query);
                yield result.toString();
            }
            case "getUpcomingActions" -> {
                int days = extractDaysParameter(input);
                var result = agentTools.getUpcomingActions(days);
                yield result.toString();
            }
            case "searchDocumentContent" -> {
                String[] parts = query.split("\\|");
                if (parts.length >= 2) {
                    var result = agentTools.searchDocumentContent(parts[0], parts[1]);
                    yield result.toString();
                }
                yield "Invalid parameters for searchDocumentContent";
            }
            default -> "Unknown tool: " + toolName;
        };
    }

    private String extractToolName(String llmResponse) {
        // Simple extraction - in production, use proper parsing
        if (llmResponse.contains("searchDocuments")) return "searchDocuments";
        if (llmResponse.contains("getDocument")) return "getDocument";
        if (llmResponse.contains("searchObligations")) return "searchObligations";
        if (llmResponse.contains("getUpcomingObligations")) return "getUpcomingObligations";
        if (llmResponse.contains("searchActions")) return "searchActions";
        if (llmResponse.contains("getUpcomingActions")) return "getUpcomingActions";
        if (llmResponse.contains("searchDocumentContent")) return "searchDocumentContent";
        return "none";
    }

    private String extractQueryParameter(String input) {
        // Simple parameter extraction - in production, use proper parsing
        if (input.contains("\"")) {
            int start = input.indexOf("\"") + 1;
            int end = input.lastIndexOf("\"");
            if (end > start) {
                return input.substring(start, end);
            }
        }
        return input;
    }

    private int extractDaysParameter(String input) {
        // Extract days parameter
        if (input.contains("days")) {
            try {
                String[] parts = input.split("[^0-9]+");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        return Integer.parseInt(part);
                    }
                }
            } catch (Exception e) {
                // Fall through
            }
        }
        return 7;  // Default to 7 days
    }

    public static class AgentExecutionResponse {
        public String executionId;
        public String answer;
        public boolean success;

        public AgentExecutionResponse(String executionId, String answer, boolean success) {
            this.executionId = executionId;
            this.answer = answer;
            this.success = success;
        }
    }
}
