package com.LifeAdmin.ai.lifeadmin.action.domain;


import com.LifeAdmin.ai.lifeadmin.common.persistence.BaseEntity;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.Priority;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.SourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * ActionItem: a task related to an obligation or created by user.
 * Requirement 19
 */
@Entity
@Table(name = "action_items", indexes = {
    @Index(name = "idx_action_items_user_id", columnList = "user_id"),
    @Index(name = "idx_action_items_obligation_id", columnList = "obligation_id")
})
public class ActionItem extends BaseEntity {

    // Getters and setters
    @Setter
    @Getter
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Setter
    @Column(name = "obligation_id")
    private UUID obligationId;

    @Setter
    @Getter
    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActionItemStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Getter
    @Column(name = "completed_at")
    private Instant completedAt;

    // Constructors
    public ActionItem() {}

    public ActionItem(UUID userId, String title, Priority priority, SourceType sourceType) {
        this.userId = userId;
        this.title = title;
        this.priority = priority;
        this.sourceType = sourceType;
        this.status = ActionItemStatus.TODO;
    }

    public void setStatus(ActionItemStatus status) {
        this.status = status;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

}
