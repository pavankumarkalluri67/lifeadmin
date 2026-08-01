package com.LifeAdmin.ai.lifeadmin.reminder.application;


import com.LifeAdmin.ai.lifeadmin.reminder.domain.Reminder;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.ReminderStatus;
import com.LifeAdmin.ai.lifeadmin.reminder.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * NotificationScheduler: processes scheduled reminders and sends notifications.
 * Requirement 20
 */
@Service
public class NotificationScheduler {

    private final ReminderRepository reminderRepository;

    public NotificationScheduler(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    /**
     * Process due IN_APP reminders every minute.
     * Requirement 20
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processDueReminders() {
        Instant now = Instant.now();
        List<Reminder> dueReminders = reminderRepository.findByStatusAndRemindAtLessThanEqual(
            ReminderStatus.SCHEDULED,
            now
        );

        for (Reminder reminder : dueReminders) {
            try {
                // Mark as sent
                reminder.setStatus(ReminderStatus.SENT);
                reminder.setSentAt(Instant.now());
                reminderRepository.save(reminder);
                
                // In production, send the actual notification via email or push
                sendNotification(reminder);
            } catch (Exception e) {
                // Mark as failed
                reminder.setStatus(ReminderStatus.FAILED);
                reminder.setFailureReason(e.getMessage());
                reminderRepository.save(reminder);
            }
        }
    }

    private void sendNotification(Reminder reminder) {
        // TODO: Implement actual notification sending (email, push, SMS, etc.)
        // For now, just log it
    }
}
