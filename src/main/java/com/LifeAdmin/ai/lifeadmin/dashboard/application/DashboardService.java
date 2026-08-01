package com.LifeAdmin.ai.lifeadmin.dashboard.application;




import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItemStatus;
import com.LifeAdmin.ai.lifeadmin.action.repository.ActionItemRepository;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.obligation.repository.ObligationRepository;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.ReminderStatus;
import com.LifeAdmin.ai.lifeadmin.reminder.repository.ReminderRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

/**
 * Dashboard_Service: dashboard summary.
 * Requirement 21
 */
@Service
public class DashboardService {

    private final ObligationRepository obligationRepository;
    private final ActionItemRepository actionItemRepository;
    private final ReminderRepository reminderRepository;
    private final CurrentUserProvider currentUserProvider;

    public DashboardService(ObligationRepository obligationRepository,
                           ActionItemRepository actionItemRepository,
                           ReminderRepository reminderRepository,
                           CurrentUserProvider currentUserProvider) {
        this.obligationRepository = obligationRepository;
        this.actionItemRepository = actionItemRepository;
        this.reminderRepository = reminderRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        UUID userId = currentUserProvider.getUserId();

        // Count upcoming obligations
        long upcomingObligations = obligationRepository.countUpcoming(userId);

        // Count obligations requiring confirmation
        long confirmationRequired = obligationRepository.countRequiringConfirmation(userId);

        // Count open action items
        long openActions = actionItemRepository.countByUserIdAndStatus(userId, ActionItemStatus.TODO);

        // Count scheduled reminders
        long scheduledReminders = reminderRepository.countByUserIdAndStatus(userId, ReminderStatus.SCHEDULED);

        return new DashboardSummary(upcomingObligations, confirmationRequired, openActions, scheduledReminders);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummary {
        private long upcomingObligations;
        private long obligationsRequiringConfirmation;
        private long openActionItems;
        private long scheduledReminders;
    }
}
