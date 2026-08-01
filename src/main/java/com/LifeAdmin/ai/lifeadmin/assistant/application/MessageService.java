package com.LifeAdmin.ai.lifeadmin.assistant.application;



import com.LifeAdmin.ai.lifeadmin.agent.repository.AgentExecutionRepository;
import com.LifeAdmin.ai.lifeadmin.assistant.domain.Message;
import com.LifeAdmin.ai.lifeadmin.assistant.domain.MessageRole;
import com.LifeAdmin.ai.lifeadmin.assistant.repository.ConversationRepository;
import com.LifeAdmin.ai.lifeadmin.assistant.repository.MessageRepository;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Message_Service: manages messages within conversations.
 * Requirement 23
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final AgentExecutionRepository agentExecutionRepository;
    private final CurrentUserProvider currentUserProvider;

    public MessageService(MessageRepository messageRepository,
                         ConversationRepository conversationRepository,
                         AgentExecutionRepository agentExecutionRepository,
                         CurrentUserProvider currentUserProvider) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.agentExecutionRepository = agentExecutionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Message createUserMessage(UUID conversationId, String content) {
        UUID userId = currentUserProvider.getUserId();
        
        // Verify conversation ownership
        conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Message message = new Message(conversationId, MessageRole.USER, content);
        return messageRepository.save(message);
    }

    @Transactional
    public Message createAssistantMessage(UUID conversationId, String content, UUID agentExecutionId) {
        UUID userId = currentUserProvider.getUserId();
        
        // Verify conversation ownership
        conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Message message = new Message(conversationId, MessageRole.ASSISTANT, content);
        if (agentExecutionId != null) {
            message.setAgentExecutionId(agentExecutionId);
        }
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<Message> listMessages(UUID conversationId, Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        
        // Verify conversation ownership
        conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        return messageRepository.findByConversationId(conversationId, pageable);
    }
}
