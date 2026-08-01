package com.LifeAdmin.ai.lifeadmin.assistant.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMessageRequest {
    private String content;
}
