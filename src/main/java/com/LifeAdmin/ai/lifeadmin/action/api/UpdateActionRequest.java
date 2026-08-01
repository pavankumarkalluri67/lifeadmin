package com.LifeAdmin.ai.lifeadmin.action.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateActionRequest {
    private String title;
    private String priority;
}
