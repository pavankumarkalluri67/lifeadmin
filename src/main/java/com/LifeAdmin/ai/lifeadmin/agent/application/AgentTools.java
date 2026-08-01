package com.LifeAdmin.ai.lifeadmin.agent.application;


import com.LifeAdmin.ai.lifeadmin.action.repository.ActionItemRepository;
import com.LifeAdmin.ai.lifeadmin.agent.domain.ToolExecutionResult;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentContentsRepository;
import com.LifeAdmin.ai.lifeadmin.document.repository.DocumentRepository;
import com.LifeAdmin.ai.lifeadmin.obligation.repository.ObligationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agent Tools: 7 tools available to the agent for querying user data.
 * All tools execute in Java with server-side userId validation (never LLM-supplied user_id).
 * Requirement 24
 */
@Component
public class AgentTools {

    private final DocumentRepository documentRepository;
    private final DocumentContentsRepository documentContentsRepository;
    private final ObligationRepository obligationRepository;
    private final ActionItemRepository actionItemRepository;
    private final CurrentUserProvider currentUserProvider;

    public AgentTools(DocumentRepository documentRepository,
                     DocumentContentsRepository documentContentsRepository,
                     ObligationRepository obligationRepository,
                     ActionItemRepository actionItemRepository,
                     CurrentUserProvider currentUserProvider) {
        this.documentRepository = documentRepository;
        this.documentContentsRepository = documentContentsRepository;
        this.obligationRepository = obligationRepository;
        this.actionItemRepository = actionItemRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Tool 1: Search documents by name or content.
     */
    public ToolExecutionResult searchDocuments(String query) {
        UUID userId = currentUserProvider.getUserId();
        try {
            var results = documentRepository.findByUserId(userId, PageRequest.of(0, 10))
                .stream()
                .filter(doc -> doc.getOriginalFileName().toLowerCase().contains(query.toLowerCase()))
                .map(doc -> doc.getOriginalFileName())
                .collect(Collectors.toList());
            return new ToolExecutionResult(results);
        } catch (Exception e) {
            return new ToolExecutionResult("Error searching documents: " + e.getMessage());
        }
    }

    /**
     * Tool 2: Get specific document details.
     */
    public ToolExecutionResult getDocument(String documentId) {
        UUID userId = currentUserProvider.getUserId();
        try {
            UUID id = UUID.fromString(documentId);
            var doc = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
            
            return new ToolExecutionResult(new DocumentInfo(
                doc.getId().toString(),
                doc.getOriginalFileName(),
                doc.getDocumentType(),
                doc.getProcessingStatus().toString(),
                doc.getUploadedAt()
            ));
        } catch (Exception e) {
            return new ToolExecutionResult("Error retrieving document: " + e.getMessage());
        }
    }

    /**
     * Tool 3: Search obligations by type, title, or status.
     */
    public ToolExecutionResult searchObligations(String query) {
        UUID userId = currentUserProvider.getUserId();
        try {
            var results = obligationRepository.findByUserId(userId, PageRequest.of(0, 10))
                .stream()
                .filter(obl -> obl.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                              obl.getType().toLowerCase().contains(query.toLowerCase()))
                .map(obl -> obl.getTitle() + " (" + obl.getType() + ")")
                .collect(Collectors.toList());
            return new ToolExecutionResult(results);
        } catch (Exception e) {
            return new ToolExecutionResult("Error searching obligations: " + e.getMessage());
        }
    }

    /**
     * Tool 4: Get upcoming obligations due within N days.
     */
    public ToolExecutionResult getUpcomingObligations(int days) {
        UUID userId = currentUserProvider.getUserId();
        try {
            LocalDate now = LocalDate.now();
            LocalDate futureDate = now.plusDays(days);
            
            var results = obligationRepository.findByUserId(userId, PageRequest.of(0, 20))
                .stream()
                .filter(obl -> {
                    LocalDate dueDate = obl.getDueDate();
                    return dueDate != null && !dueDate.isBefore(now) && !dueDate.isAfter(futureDate);
                })
                .map(obl -> obl.getTitle() + " - Due: " + obl.getDueDate())
                .collect(Collectors.toList());
            return new ToolExecutionResult(results);
        } catch (Exception e) {
            return new ToolExecutionResult("Error retrieving upcoming obligations: " + e.getMessage());
        }
    }

    /**
     * Tool 5: Search action items.
     */
    public ToolExecutionResult searchActions(String query) {
        UUID userId = currentUserProvider.getUserId();
        try {
            var results = actionItemRepository.findByUserId(userId, PageRequest.of(0, 10))
                .stream()
                .filter(action -> action.getTitle().toLowerCase().contains(query.toLowerCase()))
                .map(action -> action.getTitle() + " (" + action.getStatus() + ")")
                .collect(Collectors.toList());
            return new ToolExecutionResult(results);
        } catch (Exception e) {
            return new ToolExecutionResult("Error searching actions: " + e.getMessage());
        }
    }

    /**
     * Tool 6: Get upcoming action items.
     */
    public ToolExecutionResult getUpcomingActions(int days) {
        UUID userId = currentUserProvider.getUserId();
        try {
            var results = actionItemRepository.findByUserId(userId, PageRequest.of(0, 20))
                .stream()
                .filter(action -> action.getCompletedAt() == null)
                .map(action -> action.getTitle() + " (" + action.getPriority() + " priority)")
                .collect(Collectors.toList());
            return new ToolExecutionResult(results);
        } catch (Exception e) {
            return new ToolExecutionResult("Error retrieving upcoming actions: " + e.getMessage());
        }
    }

    /**
     * Tool 7: Search within document content.
     */
    public ToolExecutionResult searchDocumentContent(String documentId, String query) {
        UUID userId = currentUserProvider.getUserId();
        try {
            UUID id = UUID.fromString(documentId);
            var doc = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

            var contents = documentContentsRepository.findByDocumentId(id)
                .orElseThrow(() -> new IllegalArgumentException("Document content not found"));

            String content = contents.getNormalizedText();
            if (content.toLowerCase().contains(query.toLowerCase())) {
                // Return a snippet around the match
                int index = content.toLowerCase().indexOf(query.toLowerCase());
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + query.length() + 50);
                String snippet = content.substring(start, end);
                return new ToolExecutionResult("Found in document: ..." + snippet + "...");
            } else {
                return new ToolExecutionResult("Query not found in document");
            }
        } catch (Exception e) {
            return new ToolExecutionResult("Error searching document content: " + e.getMessage());
        }
    }

    // Helper class for document info
    public static class DocumentInfo {
        public String id;
        public String fileName;
        public String documentType;
        public String processingStatus;
        public Object uploadedAt;

        public DocumentInfo(String id, String fileName, String documentType, 
                           String processingStatus, Object uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.documentType = documentType;
            this.processingStatus = processingStatus;
            this.uploadedAt = uploadedAt;
        }
    }
}
