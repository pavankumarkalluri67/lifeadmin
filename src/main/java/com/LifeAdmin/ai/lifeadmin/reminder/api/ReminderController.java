package com.LifeAdmin.ai.lifeadmin.reminder.api;


import com.LifeAdmin.ai.lifeadmin.reminder.application.ReminderService;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.Reminder;
import com.LifeAdmin.ai.lifeadmin.reminder.domain.ReminderChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * ReminderController: manages reminders.
 * Requirement 20
 */
@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<Reminder> create(@RequestBody CreateReminderRequest request) {
        UUID obligationId = request.getObligationId() != null ? UUID.fromString(request.getObligationId()) : null;
        UUID actionItemId = request.getActionItemId() != null ? UUID.fromString(request.getActionItemId()) : null;
        ReminderChannel channel = ReminderChannel.valueOf(request.getChannel());
        
        Reminder reminder = reminderService.create(
            Instant.parse(request.getRemindAt()),
            channel,
            obligationId,
            actionItemId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    @GetMapping
    public ResponseEntity<Page<Reminder>> list(Pageable pageable) {
        Page<Reminder> reminders = reminderService.list(pageable);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/{reminderId}")
    public ResponseEntity<Reminder> get(@PathVariable UUID reminderId) {
        Reminder reminder = reminderService.get(reminderId);
        return ResponseEntity.ok(reminder);
    }

    @PutMapping("/{reminderId}")
    public ResponseEntity<Reminder> update(@PathVariable UUID reminderId, @RequestBody UpdateReminderRequest request) {
        ReminderChannel channel = request.getChannel() != null ? ReminderChannel.valueOf(request.getChannel()) : null;
        Instant remindAt = request.getRemindAt() != null ? Instant.parse(request.getRemindAt()) : null;
        Reminder reminder = reminderService.update(reminderId, remindAt, channel);
        return ResponseEntity.ok(reminder);
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> delete(@PathVariable UUID reminderId) {
        reminderService.delete(reminderId);
        return ResponseEntity.noContent().build();
    }
}
