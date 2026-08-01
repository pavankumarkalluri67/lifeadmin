package com.LifeAdmin.ai.lifeadmin.assistant;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AssistantServiceIntegrationTest {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void testCreateConversation_Success() {
        // Act
        Conversation conversation = assistantService.createConversation();

        // Assert
        assertNotNull(conversation);
        assertNotNull(conversation.getId());
        assertEquals("ACTIVE", conversation.getStatus().toString());
    }

    @Test
    public void testPostMessage_CreatesUserAndAssistantMessages() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Conversation conversation = assistantService.createConversation();

        // Act
        Message userMsg = messageService.createUserMessage(conversation.getId(), "What are my obligations?");
        Message assistantMsg = messageService.createAssistantMessage(
            conversation.getId(),
            "I found 5 pending obligations...",
            UUID.randomUUID()
        );

        // Assert
        assertEquals(MessageRole.USER, userMsg.getRole());
        assertEquals(MessageRole.ASSISTANT, assistantMsg.getRole());
        
        var messages = messageRepository.findByConversationId(conversation.getId(),
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, messages.getTotalElements());
    }

    @Test
    public void testArchiveConversation_Success() {
        // Arrange
        Conversation conversation = assistantService.createConversation();

        // Act
        assistantService.archiveConversation(conversation.getId());

        // Assert
        Conversation archived = conversationRepository.findById(conversation.getId()).orElse(null);
        assertNotNull(archived);
        assertEquals("ARCHIVED", archived.getStatus().toString());
    }

    @Test
    public void testGetConversation_InvalidId_ThrowsException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> assistantService.getConversation(UUID.randomUUID()));
    }
}
