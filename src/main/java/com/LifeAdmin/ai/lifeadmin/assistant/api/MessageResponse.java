package com.LifeAdmin.ai.lifeadmin.assistant.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String userMessage;
    private String assistantMessage;
    private boolean success;
}
