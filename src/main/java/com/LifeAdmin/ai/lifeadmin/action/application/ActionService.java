package com.LifeAdmin.ai.lifeadmin.action.application;

import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItem;
import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItemStatus;
import com.LifeAdmin.ai.lifeadmin.action.repository.ActionItemRepository;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.Priority;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.SourceType;
import com.LifeAdmin.ai.lifeadmin.obligation.repository.ObligationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Action_Service: action item management.
 * Requirement 19
 */
@Service
public class ActionService {

    private final ActionItemRepository actionItemRepository;
    private final ObligationRepository obligationRepository;
    private final CurrentUserProvider currentUserProvider;

    public ActionService(ActionItemRepository actionItemRepository,
                        ObligationRepository obligationRepository,
                        CurrentUserProvider currentUserProvider) {
        this.actionItemRepository = actionItemRepository;
        this.obligationRepository = obligationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public ActionItem create(String title, Priority priority, UUID obligationId) {
        UUID userId = currentUserProvider.getUserId();

        // If linking to obligation, verify ownership
        if (obligationId != null) {
            obligationRepository.findByIdAndUserId(obligationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligation not found"));
        }

        ActionItem actionItem = new ActionItem(userId, title, priority, SourceType.USER);
        if (obligationId != null) {
            actionItem.setObligationId(obligationId);
        }

        return actionItemRepository.save(actionItem);
    }

    @Transactional(readOnly = true)
    public Page<ActionItem> list(Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        return actionItemRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public ActionItem get(UUID actionId) {
        UUID userId = currentUserProvider.getUserId();
        return actionItemRepository.findByIdAndUserId(actionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Action item not found"));
    }

    @Transactional
    public ActionItem complete(UUID actionId) {
        ActionItem actionItem = get(actionId);
        actionItem.setStatus(ActionItemStatus.COMPLETED);
        actionItem.setCompletedAt(Instant.now());
        return actionItemRepository.save(actionItem);
    }

    @Transactional
    public ActionItem update(UUID actionId, String title, Priority priority) {
        ActionItem actionItem = get(actionId);
        if (title != null && !title.isBlank()) {
            actionItem.setTitle(title);
        }
        if (priority != null) {
            actionItem.setPriority(priority);
        }
        return actionItemRepository.save(actionItem);
    }

    @Transactional
    public void delete(UUID actionId) {
        ActionItem actionItem = get(actionId);
        actionItemRepository.delete(actionItem);
    }
}
