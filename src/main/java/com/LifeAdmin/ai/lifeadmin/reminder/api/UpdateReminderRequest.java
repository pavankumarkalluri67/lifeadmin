package com.LifeAdmin.ai.lifeadmin.reminder.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReminderRequest {
    private String remindAt;
    private String channel;
}
