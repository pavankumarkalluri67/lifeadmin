package com.LifeAdmin.ai.lifeadmin.assistant.application;


import com.LifeAdmin.ai.lifeadmin.assistant.domain.Conversation;
import com.LifeAdmin.ai.lifeadmin.assistant.domain.ConversationStatus;
import com.LifeAdmin.ai.lifeadmin.assistant.repository.ConversationRepository;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Assistant_Service: manages conversations with the AI assistant.
 * Requirement 23
 */
@Service
public class AssistantService {

    private final ConversationRepository conversationRepository;
    private final CurrentUserProvider currentUserProvider;

    public AssistantService(ConversationRepository conversationRepository,
                           CurrentUserProvider currentUserProvider) {
        this.conversationRepository = conversationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Conversation createConversation() {
        UUID userId = currentUserProvider.getUserId();
        Conversation conversation = new Conversation(userId);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Page<Conversation> listConversations(Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        return conversationRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId) {
        UUID userId = currentUserProvider.getUserId();
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    @Transactional
    public void archiveConversation(UUID conversationId) {
        Conversation conversation = getConversation(conversationId);
        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversationRepository.save(conversation);
    }
}
