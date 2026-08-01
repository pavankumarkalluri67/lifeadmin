package com.LifeAdmin.ai.lifeadmin.reminder.application;



import com.LifeAdmin.ai.lifeadmin.action.repository.ActionItemRepository;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.obligation.repository.ObligationRepository;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.Reminder;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.ReminderChannel;
import com.LifeAdmin.ai.lifeadmin.reminder.repository.ReminderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Reminder_Service: reminder management and scheduling.
 * Requirement 20
 */
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ObligationRepository obligationRepository;
    private final ActionItemRepository actionItemRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReminderService(ReminderRepository reminderRepository,
                          ObligationRepository obligationRepository,
                          ActionItemRepository actionItemRepository,
                          CurrentUserProvider currentUserProvider) {
        this.reminderRepository = reminderRepository;
        this.obligationRepository = obligationRepository;
        this.actionItemRepository = actionItemRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Reminder create(Instant remindAt, ReminderChannel channel,
                           UUID obligationId, UUID actionItemId) {
        UUID userId = currentUserProvider.getUserId();

        // At least one of obligation_id or action_item_id must be provided and owned
        if (obligationId == null && actionItemId == null) {
            throw new IllegalArgumentException("Must provide either obligation_id or action_item_id");
        }

        if (obligationId != null) {
            obligationRepository.findByIdAndUserId(obligationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligation not found"));
        }

        if (actionItemId != null) {
            actionItemRepository.findByIdAndUserId(actionItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Action item not found"));
        }

        Reminder reminder = new Reminder(userId, remindAt, channel);
        if (obligationId != null) {
            reminder.setObligationId(obligationId);
        }
        if (actionItemId != null) {
            reminder.setActionItemId(actionItemId);
        }

        return reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public Page<Reminder> list(Pageable pageable) {
        UUID userId = currentUserProvider.getUserId();
        return reminderRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Reminder get(UUID reminderId) {
        UUID userId = currentUserProvider.getUserId();
        return reminderRepository.findByIdAndUserId(reminderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
    }

    @Transactional
    public Reminder update(UUID reminderId, Instant remindAt, ReminderChannel channel) {
        Reminder reminder = get(reminderId);
        if (remindAt != null) {
            reminder.setRemindAt(remindAt);
        }
        if (channel != null) {
            reminder.setChannel(channel);
        }
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void delete(UUID reminderId) {
        Reminder reminder = get(reminderId);
        reminderRepository.delete(reminder);
    }
}
