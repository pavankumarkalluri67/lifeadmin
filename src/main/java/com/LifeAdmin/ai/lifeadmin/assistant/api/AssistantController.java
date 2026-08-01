package com.LifeAdmin.ai.lifeadmin.assistant.api;


import com.LifeAdmin.ai.lifeadmin.agent.application.AgentOrchestrator;
import com.LifeAdmin.ai.lifeadmin.assistant.application.AssistantService;
import com.LifeAdmin.ai.lifeadmin.assistant.application.MessageService;
import com.LifeAdmin.ai.lifeadmin.assistant.domain.Conversation;
import com.LifeAdmin.ai.lifeadmin.assistant.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * AssistantController: manages conversations and messages with the AI assistant.
 * Requirement 23
 */
@RestController
@RequestMapping("/api/v1/conversations")
public class AssistantController {

    private final AssistantService assistantService;
    private final MessageService messageService;
    private final AgentOrchestrator agentOrchestrator;

    public AssistantController(AssistantService assistantService,
                              MessageService messageService,
                              AgentOrchestrator agentOrchestrator) {
        this.assistantService = assistantService;
        this.messageService = messageService;
        this.agentOrchestrator = agentOrchestrator;
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation() {
        Conversation conversation = assistantService.createConversation();
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping
    public ResponseEntity<Page<Conversation>> listConversations(Pageable pageable) {
        Page<Conversation> conversations = assistantService.listConversations(pageable);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversation(@PathVariable UUID conversationId) {
        Conversation conversation = assistantService.getConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> postMessage(
            @PathVariable UUID conversationId,
            @RequestBody PostMessageRequest request) {
        
        // Save user message
        Message userMessage = messageService.createUserMessage(conversationId, request.getContent());
        
        // Run agent orchestrator
        AgentOrchestrator.AgentExecutionResponse agentResponse = agentOrchestrator.run(request.getContent());
        
        // Save assistant response
        Message assistantMessage = messageService.createAssistantMessage(
            conversationId,
            agentResponse.answer,
            UUID.fromString(agentResponse.executionId)
        );
        
        MessageResponse response = new MessageResponse();
        response.setUserMessage(userMessage.getContent());
        response.setAssistantMessage(assistantMessage.getContent());
        response.setSuccess(agentResponse.success);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<Message>> listMessages(
            @PathVariable UUID conversationId,
            Pageable pageable) {
        Page<Message> messages = messageService.listMessages(conversationId, pageable);
        return ResponseEntity.ok(messages);
    }
}
