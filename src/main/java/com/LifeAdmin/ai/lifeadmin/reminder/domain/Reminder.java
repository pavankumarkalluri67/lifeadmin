package com.LifeAdmin.ai.lifeadmin.reminder.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Reminder: a scheduled notification linked to an obligation or action item.
 * Requirement 20
 */
@Entity
@Table(name = "reminders", indexes = {
    @Index(name = "idx_reminders_user_id", columnList = "user_id"),
    @Index(name = "idx_reminders_obligation_id", columnList = "obligation_id"),
    @Index(name = "idx_reminders_action_item_id", columnList = "action_item_id")
})
public class Reminder extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "obligation_id")
    private UUID obligationId;

    @Column(name = "action_item_id")
    private UUID actionItemId;

    @NotNull
    @Column(name = "remind_at", nullable = false)
    private Instant remindAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private ReminderChannel channel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReminderStatus status;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Size(max = 512)
    @Column(name = "failure_reason")
    private String failureReason;

    // Constructors
    public Reminder() {}

    public Reminder(UUID userId, Instant remindAt, ReminderChannel channel) {
        this.userId = userId;
        this.remindAt = remindAt;
        this.channel = channel;
        this.status = ReminderStatus.SCHEDULED;
    }

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getObligationId() {
        return obligationId;
    }

    public void setObligationId(UUID obligationId) {
        this.obligationId = obligationId;
    }

    public UUID getActionItemId() {
        return actionItemId;
    }

    public void setActionItemId(UUID actionItemId) {
        this.actionItemId = actionItemId;
    }

    public Instant getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(Instant remindAt) {
        this.remindAt = remindAt;
    }

    public ReminderChannel getChannel() {
        return channel;
    }

    public void setChannel(ReminderChannel channel) {
        this.channel = channel;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
