package com.LifeAdmin.ai.lifeadmin.action.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateActionRequest {
    private String title;
    private String priority;
    private String obligationId;
}
